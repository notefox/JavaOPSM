package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStopException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class SimpleProcessRunner implements ProcessRunner {

    private final ProcessBuilder pb;
    private Process process;

    public SimpleProcessRunner(List<String> processCommand) {
        this.pb = new ProcessBuilder();
        pb.command(processCommand);
    }
    public SimpleProcessRunner(ProcessBuilder pb) {
        this.pb = pb;
    }
    public SimpleProcessRunner(String processCommand) {
        this.pb = new ProcessBuilder();
        pb.command(processCommand);
    }

    @Override
    public void startProcess() throws IOException, ProcessCouldNotStartException, ProcessAlreadyStartedException {
        if (process != null && process.isAlive()) {
            throw new ProcessAlreadyStartedException("process already running, needs to be stopped first before start " +
                    "or if you want to restart, use the restart Method");
        }
        process = pb.start();
        if (!process.isAlive()) {
            throw new ProcessCouldNotStartException("process was either too fast done than the " +
                    "check could have been performed or it did not start\n " +
                    "if you are 100% sure that the process should have started, " +
                    "try calling startProcessWithoutRunningStartTest()");
        }
        afterStartProcessEvent();
    }

    @Override
    public void startProcessWithoutRunningStartTest() throws IOException, ProcessAlreadyStartedException {
        if (process != null && process.isAlive()) {
            throw new ProcessAlreadyStartedException("process already running, needs to be stopped first before start " +
                    "or if you want to restart, use the restart Method");
        }
        process = pb.start();
        afterStartProcessEvent();
    }

    /**
     * overridable event method called after start method was called
     */
    protected abstract void afterStartProcessEvent();

    @Override
    public void stopProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException {
        if (!process.isAlive()) {
            throw new ProcessIsNotAliveException();
        }
        process.destroy();
        if (process.isAlive()) {
            throw new ProcessCouldNotStopException();
        }
        afterStopProcessEvent();
    }

    /**
     * overrideable event method called after stop method was called
     */
    protected abstract void afterStopProcessEvent();

    @Override
    public void restartProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, IOException,
            ProcessCouldNotStartException, ProcessAlreadyStartedException {
        stopProcess();
        startProcess();
        afterRestartProcessEvent();
    }

    @Override
    public void restartProcessWithoutRunningStartTest() throws ProcessIsNotAliveException,
            ProcessCouldNotStopException, IOException, ProcessAlreadyStartedException {
        stopProcess();
        startProcessWithoutRunningStartTest();
        afterRestartProcessEvent();
    }

    protected abstract void afterRestartProcessEvent();

    @Override
    public boolean isProcessAlive() {
        if (process == null) {
            return false;
        }
        return process.isAlive();
    }

    @Override
    public void waitForProcess() throws InterruptedException {
        if (!isProcessAlive()) {
            return;
        }
        process.waitFor();
    }

    @Override
    public long getPID() throws ProcessIsNotAliveException {
        if (!isProcessAlive()) {
            throw new ProcessIsNotAliveException();
        }
        return process.pid();
    }

    @Override
    public ProcessHandle.Info getProcessInfo() {
        if (process == null) {
            throw new NullPointerException();
        }
        return process.info();
    }

    @Override
    public List<String> getCommand() {
        return pb.command();
    }

    /**
     * returns process input stream
     * @return OutputStream
     */
    public InputStream getProcessInputStream() {
        if (process == null) {
            throw new NullPointerException();
        }
        return process.getInputStream();
    }

    /**
     * returns process error stream
     * @return InputStream
     */
    public InputStream getProcessErrorStream() {
        if (process == null) {
            throw new NullPointerException();
        }
        return process.getErrorStream();
    }
}

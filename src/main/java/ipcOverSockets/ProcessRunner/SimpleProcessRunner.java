package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStopException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;

import java.io.IOException;
import java.util.List;

public abstract class SimpleProcessRunner implements ProcessRunner {

    private final ProcessBuilder pb = new ProcessBuilder();
    private Process process;

    public SimpleProcessRunner(List<String> processCommand) {
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
            throw new ProcessCouldNotStartException();
        }
        afterStartProcessEvent();
    }
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
    protected abstract void afterStopProcessEvent();

    @Override
    public void restartProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, IOException,
            ProcessCouldNotStartException, ProcessAlreadyStartedException {
        stopProcess();
        startProcess();
    }

    @Override
    public boolean isProcessAlive() {
        if (process == null) {
            return false;
        }
        return process.isAlive();
    }

    @Override
    public void waitForProcess() throws ProcessIsNotAliveException, InterruptedException {
        if (!isProcessAlive()) {
            throw new ProcessIsNotAliveException();
        }
        process.waitFor();
    }
}

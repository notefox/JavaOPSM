package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class SimpleProcessRunner implements ProcessRunner {

    /**
     * type of runner
     */
    private ProcessRunnerType runnerType = ProcessRunnerType.CUSTOM;

    /**
     * process name
     */
    private final String name;

    /**
     * process builder
     */
    private final ProcessBuilder pb;

    /**
     * currently/last running process
     */
    private Process process;

    /**
     * simple Constructor with a command List
     * @param processCommand commandList
     */
    public SimpleProcessRunner(@NotNull String name, ProcessRunnerType type, @NotNull List<String> processCommand) {
        this.runnerType = type;
        this.name = name;
        this.pb = new ProcessBuilder();
        pb.command(processCommand);
    }

    /**
     * constructor for direct ProcessBuilderInjection
     * @param pb ProcessBuilder
     */
    public SimpleProcessRunner(@NotNull String name, ProcessRunnerType type, @NotNull ProcessBuilder pb) {
        this.name = name;
        this.pb = pb;
        this.runnerType = type;
    }

    /**
     * single command process constructor
     * @param processCommand command
     */
    public SimpleProcessRunner(@NotNull String name, ProcessRunnerType type, @NotNull String processCommand) {
        this.runnerType = type;
        this.name = name;
        this.pb = new ProcessBuilder();
        pb.command(processCommand);
    }

    /**
     * Strings of process commands, directly used for ProcessBuilder
     * @param processCommands commands and arguments
     */
    public SimpleProcessRunner(@NotNull String name, ProcessRunnerType type, @NotNull String... processCommands) {
        this.runnerType = type;
        this.name = name;
        this.pb = new ProcessBuilder();
        if (processCommands.length == 0) {
            throw new NullPointerException("no process command were given");
        } else {
            this.pb.command(processCommands);
        }
    }

    /**
     * ProcessBuilder reconstructor Constructor
     * this only works if the Process is still running
     *
     * (the Process will continue running till the end)
     *
     * @param p Process to reproduce
     * @throws ProcessCouldNotBeReproducedException is thrown, if the process wasn't usable or already died
     */
    public SimpleProcessRunner(@NotNull String name, Process p) throws ProcessCouldNotBeReproducedException {
        this.name = name;
        if (p.info().command().isPresent() || p.info().arguments().isPresent()) {
            String command = p.info().command().get();
            String[] arguments = p.info().arguments().get();
            ProcessBuilder reproduced = new ProcessBuilder();
            List<String> commandList = new ArrayList<>();
            commandList.add(command);
            commandList.addAll(Arrays.asList(arguments));
            reproduced.command(commandList);

            this.pb = reproduced;
            this.process = p;
        } else {
            throw new ProcessCouldNotBeReproducedException("process wasn't reproducible because either the process wasn't " +
                    "usable or the process already died before it could have been reproduced");
        }
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
        new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            afterFinishProcessEvent();
        }).start();

    }

    @Override
    public void startProcessWithoutRunningStartTest() throws IOException, ProcessAlreadyStartedException {
        if (process != null && process.isAlive()) {
            throw new ProcessAlreadyStartedException("process already running, needs to be stopped first before start " +
                    "or if you want to restart, use the restart Method");
        }
        process = pb.start();
        afterStartProcessEvent();
        new Thread(() -> {
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            afterFinishProcessEvent();
        }).start();
    }

    /**
     * overridable event method called after start method was called successfully
     */
    protected abstract void afterStartProcessEvent();

    @Override
    public void stopProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException {
        if (!isProcessAlive()) {
            throw new ProcessIsNotAliveException();
        }
        process.destroy();
        if (isProcessAlive()) {
            process.destroyForcibly();
            process.waitFor(15, TimeUnit.MILLISECONDS);

            if (isProcessAlive()) {
                throw new ProcessCouldNotStopException();
            }
        }
        afterStopProcessEvent();
    }

    /**
     * overrideable event method called after stop method was called successfully
     */
    protected abstract void afterStopProcessEvent();

    @Override
    public void restartProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, IOException,
            ProcessCouldNotStartException, ProcessAlreadyStartedException, InterruptedException {
        stopProcess();
        startProcess();
        afterRestartProcessEvent();
    }

    @Override
    public void restartProcessWithoutRunningStartTest() throws ProcessIsNotAliveException,
            ProcessCouldNotStopException, IOException, ProcessAlreadyStartedException, InterruptedException {
        stopProcess();
        startProcessWithoutRunningStartTest();
        afterRestartProcessEvent();
    }

    /**
     * overrideable event method called after restart method was called successfully
     */
    protected abstract void afterRestartProcessEvent();

    /**
     * overrideable event method called after process finished without being stopped
     */
    protected abstract void afterFinishProcessEvent();

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
    public void waitForProcess(long time) throws ProcessIsNotAliveException, InterruptedException, ProcessCouldNotStopException {
        if (!isProcessAlive()) {
            return;
        }
        process.waitFor(time, TimeUnit.MILLISECONDS);
        if (this.isProcessAlive())
            this.stopProcess();
    }

    @Override
    public int getLastExitCode() throws ProcessNotExitedYetException {
        try {
            return process.exitValue();
        } catch (IllegalThreadStateException e) {
            throw new ProcessNotExitedYetException(e);
        }
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

    /**
     * process name getter
     * @return String
     */
    public String getName() {
        return name;
    }

    public ProcessBuilder getProcessBuilder() {
        return new ProcessBuilder(pb.command());
    }

    public ProcessRunnerType getType() {
        return runnerType;
    }

    @Override
    public String toString() {
        return "SimpleProcessRunner{" +
                "name='" + name + '\'' +
                ", pb=" + pb.command() +
                ", process=" + process +
                '}';
    }
}

package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class DirectCommandRunner extends SimpleProcessRunner {

    public DirectCommandRunner(@NotNull String processCommand, PrintStream output, PrintStream error, File logDir) throws IOException, ProcessAlreadyStartedException, InterruptedException {
        super(processCommand, ProcessRunnerType.DIRECT_COMMAND_RUNNER, processCommand, logDir);
        this.startProcessWithoutRunningStartTest();
        this.waitForProcess();
        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));

        while (br.ready()) {
            output.println(br.readLine());
        }

        while (be.ready()) {
            error.println(be.readLine());
        }
    }

    @Override
    protected void afterStartProcessEvent() {
        //
    }

    @Override
    protected void afterStopProcessEvent() {
        //
    }

    @Override
    protected void afterRestartProcessEvent() {
        //
    }

    @Override
    protected void afterFinishProcessEvent() {

    }
}

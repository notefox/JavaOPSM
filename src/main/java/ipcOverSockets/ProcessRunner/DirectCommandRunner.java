package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class DirectCommandRunner extends SimpleProcessRunner {

    public DirectCommandRunner(@NotNull String processCommand, PrintStream output, PrintStream error) throws IOException, ProcessAlreadyStartedException, InterruptedException {
        super(processCommand, ProcessRunnerType.DIRECT_COMMAND_RUNNER, processCommand);
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

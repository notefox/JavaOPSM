package processManagement.processRunner;

import logger.LogType;
import processManagement.processExceptions.ProcessAlreadyStartedException;

import org.jetbrains.annotations.NotNull;
import processManagement.processRunner.constructObject.ProcessRunnerType;
import processManagement.processRunner.constructObject.SimpleProcessRunner;

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
        log(LogType.INFO, "running " + this.getCommand().get(0));
    }

    @Override
    protected void afterStopProcessEvent() {
        log(LogType.INFO, "stopped " + this.getCommand().get(0));
    }

    @Override
    protected void afterRestartProcessEvent() {
        log(LogType.INFO, "restarted " + this.getCommand().get(0));
    }

    @Override
    protected void afterFinishProcessEvent() {
        log(LogType.INFO, "finished " + this.getCommand().get(0));
    }
}

package ipcOverSockets.ProcessRunnerTemplates;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessRunner.ScriptCreator;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;

import java.io.IOException;
import java.util.ArrayList;

public class PythonScriptTemplate {
    public static void main(String[] args) {
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("python");
        commandList.add("modules/SingleFile/Python/singleFilePythonExample.py");
        commandList.add("someFile.txt");
        SimpleProcessRunner spr = new SimpleProcessRunner(commandList) {
            @Override
            protected void afterStartProcessEvent() {
            }

            @Override
            protected void afterStopProcessEvent() {
            }

            @Override
            protected void afterRestartProcessEvent() {

            }
        };
        try {
            spr.startProcess();
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
            e.printStackTrace();
        }
    }
}

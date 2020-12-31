package ipcOverSockets.ProcessRunnerTemplates;

import ipcOverSockets.ProcessRunner.ProcessRunnerType;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;

import java.util.ArrayList;

public class PythonScriptTemplate {
    public static SimpleProcessRunner buildTemplate() {
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("singleFile/python");
        commandList.add("modules/SingleFile/Python/singleFilePythonExample.py");
        commandList.add("someFile.txt");
        SimpleProcessRunner spr = new SimpleProcessRunner("python template", ProcessRunnerType.SCRIPT_RUNNER, commandList) {
            @Override
            protected void afterStartProcessEvent() {
            }

            @Override
            protected void afterStopProcessEvent() {
            }

            @Override
            protected void afterRestartProcessEvent() {

            }

            @Override
            protected void afterFinishProcessEvent() {

            }
        };
        return spr;
    }
}

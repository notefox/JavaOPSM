package processManagement.processRunnerTemplates;

import processManagement.processRunner.constructObject.ProcessRunnerType;
import processManagement.processRunner.constructObject.SimpleProcessRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PythonScriptTemplate {
    public static SimpleProcessRunner buildTemplate() throws IOException {
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("singleFile/python");
        commandList.add("modules/SingleFile/Python/singleFilePythonExample.py");
        commandList.add("someFile.txt");
        SimpleProcessRunner spr = new SimpleProcessRunner("python template", ProcessRunnerType.SCRIPT_RUNNER, commandList, new File("logger")) {
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

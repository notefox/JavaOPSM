package ipcOverSockets.ProcessRunnerTemplates;

import ipcOverSockets.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessRunner.ScriptCreator;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class JavaMavenRunnerTemplate {
    public static void main(String[] args) {
        String pathToMVNProject = "modules/Projects/mavenExampleProject";

        // create script file
        File script = new File("scripts/mvn_compile_script.sh");
        ScriptCreator sc = new ScriptCreator("bash", script) {
            @Override
            public void afterRun(Process process) {
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader be = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                try {
                    while (br.ready()) {
                        System.out.println(br.readLine());
                    }
                    while (be.ready()) {
                        System.err.println(be.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // create necessary script
        sc.addLineToScript("cd " + pathToMVNProject + " || exit");
        sc.addLineToScript("mvn install\n");
        sc.addLineToScript("java -jar target/mavenExampleProject-0.1.jar");

        SimpleProcessRunner spc = null;
        try {
            ProcessBuilder pb = sc.buildRunnableProcessBuilder();
            spc = new SimpleProcessRunner(pb) {
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
            };
        } catch (IOException | InterpreterOrScriptNotDefinedException e) {
            e.printStackTrace();
        }

        try {
            assert spc != null;
            spc.startProcess();
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
            e.printStackTrace();
        }

        try {
            spc.waitForProcess();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

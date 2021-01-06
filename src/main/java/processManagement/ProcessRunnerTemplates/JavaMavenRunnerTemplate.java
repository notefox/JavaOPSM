package processManagement.ProcessRunnerTemplates;

import processManagement.ProcessExceptions.ExecutableFileInRootDirectoryException;
import processManagement.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import processManagement.ProcessRunner.ProcessRunnerType;
import processManagement.ProcessRunner.ScriptCreator;
import processManagement.ProcessRunner.SimpleProcessRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class JavaMavenRunnerTemplate {
    public static SimpleProcessRunner buildTemplate() throws ExecutableFileInRootDirectoryException, IOException {
        String pathToMVNProject = "modules/Projects/mavenExampleProject";

        // create script file
        File script = new File("scripts/mvn_compile_script.sh");
        ScriptCreator sc = new ScriptCreator("bash", script, null) {
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
            spc = new SimpleProcessRunner("maven template", ProcessRunnerType.PRECOMPILE_RUNNER ,pb, new File("logger")) {
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
                    //
                }
            };
        } catch (IOException | InterpreterOrScriptNotDefinedException e) {
            e.printStackTrace();
        }
        return spc;
    }
}

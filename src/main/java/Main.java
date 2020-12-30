import ipcOverSockets.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessManager;
import ipcOverSockets.ProcessRunner.ProcessRunner;
import ipcOverSockets.ProcessRunner.ScriptCreator;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;
import ipcOverSockets.ProcessRunnerTemplates.PythonScriptTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Main {
    private static File scriptDir;
    private static File modulesDir;

    private static final ProcessManager manager = new ProcessManager();

    public static void main(String[] args) throws IOException, InterpreterOrScriptNotDefinedException, ProcessAlreadyStartedException, ProcessCouldNotStartException, InterruptedException {
        //initIntoManager();
        // create script file
        File script = new File("scripts/python_test_script.py");
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
        sc.addLineToScript("print \"hello world\"");
        sc.addLineToScript("exit(0)");

        SimpleProcessRunner spr = new SimpleProcessRunner("test", sc.buildRunnableProcessBuilder()) {
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
        spr.startProcess();
        spr.waitForProcess();

        // create necessary script
        // sc.addLineToScript("cd " + pathToMVNProject + " || exit");
        // sc.addLineToScript("mvn install\n");
        // sc.addLineToScript("java -jar target/mavenExampleProject-0.1.jar");
    }

    private static void initIntoManager() throws IOException {
        HashMap<String, HashMap<String, String>> init = InitViaFile.init(new File("init.ini"));
        //init.keySet().forEach(x -> System.out.println(x + " -> " + init.get(x)));
        readInDefaultDir(init);
        readInModuels(init);
        for (SimpleProcessRunner pr :
                manager.getAllModules()) {
            System.out.println(pr.getName());
        }
    }
    private static void readInModuels(HashMap<String, HashMap<String, String>> init) {
        init.keySet().forEach((x) -> {
              if (!x.equals("initValues")) {
                  SimpleProcessRunner spr = buildProcessRunner(init.get(x));
                  manager.addModule(spr.getName(), spr);
              }
        });
    }
    private static SimpleProcessRunner buildProcessRunner(HashMap<String, String> map) {
        map = trimKeys(map);
        String name = map.get("name");
        String file = map.get("file");
        String type = map.get("type");
        String interpreter = map.get("interpreter");
        String compiler = map.get("compiler");
        String comunication = null;
        if (map.containsKey("communication") && !map.get("communication").equals("none")) {
            comunication = map.get("communication");
        }
        String parameter = null;
        if (map.containsKey("parameter") && !map.get("parameter").equals("none")) {
            parameter = map.get("parameter");
        }
        String build = null;
        String targetJar = null;
        if (type.equals("project")) {
            build = map.get("build");
            targetJar = map.get("target_jar");
        }

        SimpleProcessRunner spr = null;
        // build the actual ProcessRunner
        switch (type) {
            case "project":
                ScriptCreator buildScript = new ScriptCreator("bash", new File(scriptDir.getPath() + "/" + name + "_builder.sh")) {
                    @Override
                    public void afterRun(Process process) {
                        // nothing
                    }
                };
                buildScript.addLineToScript("cd " + modulesDir + file);
                buildScript.addLineToScript(build);
                buildScript.addLineToScript(compiler + " " + targetJar);
                try {
                    spr = new SimpleProcessRunner(name, buildScript.buildRunnableProcessBuilder()) {
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
            break;
            case "single":
                if (interpreter != null) {
                    spr = new SimpleProcessRunner(name, interpreter, modulesDir + file, parameter) {
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
                } else if (compiler != null) {
                    spr = new SimpleProcessRunner(name, compiler, modulesDir + file, parameter) {
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
                } else {
                    throw new NullPointerException("neither compiler nor interpreter were set for " + name);
                }
            break;
            case "script":
                spr = new SimpleProcessRunner(name, interpreter, file) {
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
            break;
            default:
                throw new TypeNotPresentException("given ini type " + type + " for module named " + name +
                        "is not existent", new NullPointerException());
        }
        return spr;
    }
    private static void readInDefaultDir(HashMap<String, HashMap<String, String>> init) {
        HashMap<String, String> defaultVariables = init.get("initValues");
        defaultVariables = trimKeys(defaultVariables);
        modulesDir = new File(defaultVariables.get("modules_dir"));
        scriptDir = new File(defaultVariables.get("scripts_dir"));
    }
    private static HashMap<String, String> trimKeys(HashMap<String, String> map) {
        HashMap<String, String> trimmedMap = new HashMap<>();
        for (String key : map.keySet()) {
            trimmedMap.put(key.trim(), map.get(key));
        }
        return trimmedMap;
    }

}
import ipcOverSockets.ProcessExceptions.*;
import ipcOverSockets.ProcessManager;
import ipcOverSockets.ProcessRunner.*;
import ipcOverSockets.ProcessRunnerTemplates.PythonScriptTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Main {
    private static File scriptDir;
    private static File modulesDir;

    private static final ProcessManager manager = new ProcessManager();

    public static void main(String[] args) throws IOException, InterpreterOrScriptNotDefinedException, ProcessAlreadyStartedException, ProcessCouldNotStartException, InterruptedException, ProcessNotExitedYetException {
        initIntoManager();
        manager.getAllModules().forEach(x -> {
            try {
                System.out.println(x.toString());
                x.waitForProcess(20000L);
                BufferedReader br = new BufferedReader(new InputStreamReader(x.getProcessInputStream()));
                BufferedReader be = new BufferedReader(new InputStreamReader(x.getProcessErrorStream()));

                while (br.ready()) {
                    System.out.println(x.getName() + " normal \t" + br.readLine());
                }

                while (be.ready()) {
                    System.err.println(x.getName() + " error \t" + be.readLine());
                }
                System.out.println(" exit code : " + x.getLastExitCode());
                System.out.println("\n\n\n");
                Thread.sleep(100);
            } catch (InterruptedException | IOException | ProcessNotExitedYetException | ProcessIsNotAliveException | ProcessCouldNotStopException e) {
                e.printStackTrace();
            }
        });
    }

    private static void initIntoManager() throws IOException {
        HashMap<String, HashMap<String, String>> init = InitViaFile.init(new File("init.ini"));
        //init.keySet().forEach(x -> System.out.println(x + " -> " + init.get(x)));
        readInDefaultDir(init);
        readInModuels(init);
    }
    private static void readInModuels(HashMap<String, HashMap<String, String>> init) {
        init.keySet().forEach((x) -> {
              if (!x.equals("initValues")) {
                  SimpleProcessRunner spr = null;
                  try {
                      spr = buildProcessRunner(init.get(x));
                  } catch (ExecutableFileInRootDirectoryException e) {
                      e.printStackTrace();
                  }
                  manager.addModule(spr.getName(), spr);
                  try {
                      spr.startProcessWithoutRunningStartTest();
                  } catch (IOException | ProcessAlreadyStartedException e) {
                      e.printStackTrace();
                  }
              }
        });
    }
    private static SimpleProcessRunner buildProcessRunner(HashMap<String, String> map) throws ExecutableFileInRootDirectoryException {
        map = trimKeys(map);
        String name = map.get("name");
        String file = map.get("file");
        String type = map.get("type");
        String interpreter = map.get("interpreter");
        String compiler = map.get("compiler");

        String communication = null;
        if (map.containsKey("communication") && !map.get("communication").equals("none")) {
            communication = map.get("communication");
        }

        String parameter = null;
        if (map.containsKey("parameter") && !map.get("parameter").equals("none")) {
            parameter = map.get("parameter");
        }

        String port = null;
        if (map.containsKey("port") && !map.get("port").equals("none")) {
            port = map.get("port");
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
                    buildScript.addLineToScript("cd " + modulesDir + "/" + file + " || exit");
                    buildScript.addLineToScript(build);
                    if (parameter != null)
                        buildScript.addLineToScript(compiler + " " + targetJar + " " + parameter);
                    else
                        buildScript.addLineToScript(compiler + " " + targetJar);
                    try {
                        spr = new SimpleProcessRunner(name, ProcessRunnerType.SCRIPT_RUNNER, buildScript.buildRunnableProcessBuilder()) {
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
                    break;
                case "single":
                    ArrayList<String> commandList = new ArrayList<>();
                    ProcessRunnerType pRType;
                    if (interpreter != null) {
                        commandList.add(interpreter);
                        pRType = ProcessRunnerType.SCRIPT_RUNNER;
                    } else if (compiler != null) {
                        commandList.add(compiler);
                        pRType = ProcessRunnerType.STANDARD_RUNNER;
                    } else {
                        throw new NullPointerException("neither compiler nor interpreter were set for " + name);
                    }
                    commandList.add(modulesDir + "/" + file);
                    if (parameter != null) {
                        Collections.addAll(commandList, parameter.split(" "));
                    }
                    spr = new SimpleProcessRunner(name, pRType, commandList) {
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
                    break;
                case "script":
                    commandList = new ArrayList<>();
                    commandList.add(interpreter);
                    commandList.add(scriptDir + "/" + file);
                    if (parameter != null) {
                        Collections.addAll(commandList, parameter.split(" "));
                    }
                    spr = new SimpleProcessRunner(name, ProcessRunnerType.SCRIPT_RUNNER, commandList) {
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
                    break;
                default:
                    throw new TypeNotPresentException("given ini type " + type + " for module named " + name +
                            "is not existent", new NullPointerException());
            }

            if (communication != null) {
                switch (communication) {
                    case "socket":
                        ServerSocket svso = null;
                        try {
                            assert port != null;
                            svso = new ServerSocket(Integer.parseInt(port));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        assert spr != null;
                        assert svso != null;
                        return new SocketCommunicationProcessRunner(name, spr.getProcessBuilder(), svso);
                    case "none":
                        System.err.println("unnecessary communications token was detected and masterfully ignored");
                    default:
                        System.err.println("communication type : " + communication + " not recognizable for module : " + name);
                }
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

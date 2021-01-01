import Logger.LogType;
import ipcOverSockets.InitViaFile;
import ipcOverSockets.ProcessExceptions.*;
import ipcOverSockets.ProcessManager;
import ipcOverSockets.ProcessRunner.*;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainRuntime {
    /**
     * script path
     */
    private static File scriptDir;

    /**
     * modules path
     */
    private static File modulesDir;

    /**
     * logger path
     */
    private static File loggerDir;

    /**
     * used process manager
     */
    private static ProcessManager manager;

    /**
     * main executive method
     * @param args given arguments while execution
     */
    public static void main(String[] args) {
        try {
            initIntoManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
        manager.stopAllRunningProcesses();
        manager.runAllScripts();
        System.exit(0);
    }

    /**
     * initializer method
     * @throws IOException is thrown, if the ini file reading wasn't successful
     */
    private static void initIntoManager() throws IOException {
        HashMap<String, HashMap<String, String>> init = InitViaFile.init(new File("init.ini"));
        // ------------------ do not change this ------------------
        readInDefaultDir(init);
        try {
            initializeManager(loggerDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // --------------------------------------------------------
        readInModels(init);
    }

    /**
     * define default dirs
     * (like scriptDir, modulesDir or loggerDir)
     * @param init init file to read from
     */
    private static void readInDefaultDir(HashMap<String, HashMap<String, String>> init) {
        HashMap<String, String> defaultVariables = init.get("initValues");
        defaultVariables = trimKeys(defaultVariables);
        modulesDir = new File(defaultVariables.get("modules_dir"));
        scriptDir = new File(defaultVariables.get("scripts_dir"));
        loggerDir = new File(defaultVariables.get("logger_dir"));
    }

    /**
     * initialize local process manager
     * @param loggerDir log directory for the given processes
     * @throws IOException is thrown, if any defined logger has problems with the given file path
     */
    private static void initializeManager(File loggerDir) throws IOException {
        manager = new ProcessManager(new File(loggerDir.getPath() + "/processManager"));
    }

    /**
     * read in modules from the defined HashMap from the InitViaFile.init()
     * @param init read in init hashmap
     */
    private static void readInModels(HashMap<String, HashMap<String, String>> init) {
        init.keySet().forEach((x) -> {
            if (!x.equals("initValues")) {
                SimpleProcessRunner spr = null;
                try {
                    spr = buildProcessRunner(init.get(x));
                } catch (ExecutableFileInRootDirectoryException | IOException e) {
                    e.printStackTrace();
                }
                assert spr != null;
                manager.addModule(spr.getName(), spr);
            }
        });
    }

    /**
     * build SimpleProcessRunner from given HashMap, which was read in from the InitViaFile.init
     * @param map values map
     * @return SimpleProcessRunner build from the given HashMap
     * @throws ExecutableFileInRootDirectoryException is thrown, if the path to the given ProcessRunner file is null
     * @throws IOException is thrown, if any defined logger has problems with the given file path
     */
    private static SimpleProcessRunner buildProcessRunner(HashMap<String, String> map) throws ExecutableFileInRootDirectoryException, IOException {
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
                ScriptCreator buildScript = new ScriptCreator("bash", new File(scriptDir.getPath() + "/" + name + "_builder.sh"), loggerDir) {
                    @Override
                    public void afterRun(Process process) {
                        log(LogType.INFO, "run successful");
                    }
                };
                buildScript.addLineToScript("cd " + modulesDir + "/" + file + " || exit");
                buildScript.addLineToScript(build);
                if (parameter != null)
                    buildScript.addLineToScript(compiler + " " + targetJar + " " + parameter);
                else
                    buildScript.addLineToScript(compiler + " " + targetJar);
                try {
                    spr = new SimpleProcessRunner(name, ProcessRunnerType.PROJECT_RUNNER, buildScript.buildRunnableProcessBuilder(), loggerDir) {
                        @Override
                        protected void afterStartProcessEvent() {
                            log(LogType.INFO, "process started");
                        }

                        @Override
                        protected void afterStopProcessEvent() {
                            log(LogType.INFO, "process stopped");
                        }

                        @Override
                        protected void afterRestartProcessEvent() {
                            log(LogType.INFO, "process restarted");
                        }

                        @Override
                        protected void afterFinishProcessEvent() {
                            log(LogType.INFO, "process finished");
                        }
                    };
                } catch (InterpreterOrScriptNotDefinedException | IOException e) {
                    // TODO : add logger to MainRuntime
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
                try {
                    spr = new SimpleProcessRunner(name, pRType, commandList, loggerDir) {
                        @Override
                        protected void afterStartProcessEvent() {
                            log(LogType.INFO, "process started");
                        }

                        @Override
                        protected void afterStopProcessEvent() {
                            log(LogType.INFO, "process stopped");
                        }

                        @Override
                        protected void afterRestartProcessEvent() {
                            log(LogType.INFO, "process restarted");
                        }

                        @Override
                        protected void afterFinishProcessEvent() {
                            log(LogType.INFO, "process finished");
                        }
                    };
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "script":
                commandList = new ArrayList<>();
                commandList.add(interpreter);
                commandList.add(scriptDir + "/" + file);
                if (parameter != null) {
                    Collections.addAll(commandList, parameter.split(" "));
                }
                try {
                    spr = new SimpleProcessRunner(name, ProcessRunnerType.SCRIPT_RUNNER, commandList, loggerDir) {
                        @Override
                        protected void afterStartProcessEvent() {
                            log(LogType.INFO, "process started");
                        }

                        @Override
                        protected void afterStopProcessEvent() {
                            log(LogType.INFO, "process stopped");
                        }

                        @Override
                        protected void afterRestartProcessEvent() {
                            log(LogType.INFO, "process restarted");
                        }

                        @Override
                        protected void afterFinishProcessEvent() {
                            log(LogType.INFO, "process finished");
                        }
                    };
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                    try {
                        return new SocketCommunicationProcessRunner(name, spr.getProcessBuilder(), loggerDir, svso);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                case "none":
                    System.err.println("unnecessary communications token was detected and masterfully ignored");
                default:
                    System.err.println("communication type : " + communication + " not recognizable for module : " + name);
            }
        }
        return spr;
    }


    /**
     * method trims keys of HashMap
     * @param map map to trim
     * @return map trimmed
     */
    private static HashMap<String, String> trimKeys(HashMap<String, String> map) {
        HashMap<String, String> trimmedMap = new HashMap<>();
        for (String key : map.keySet()) {
            trimmedMap.put(key.trim(), map.get(key));
        }
        return trimmedMap;
    }

}

package initialisation;

import logger.LogType;
import org.jetbrains.annotations.NotNull;
import processManagement.processExceptions.ExecutableFileInRootDirectoryException;
import processManagement.processExceptions.InterpreterOrScriptNotDefinedException;
import processManagement.ProcessManager;
import processManagement.processRunner.constructObject.ProcessRunnerType;
import processManagement.processRunner.constructObject.ScriptCreator;
import processManagement.processRunner.constructObject.SimpleProcessRunner;
import processManagement.processRunner.SocketCommunicationProcessRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ManagerFiller {

    private static File loggerDir;
    private static File scriptDir;
    private static File modulesDir;

    public static void fillManager(HashMap<String, HashMap<String, String>> init, ProcessManager manager, File loggerDir, File scriptDir, File modulesDir) {
        ManagerFiller.loggerDir = loggerDir;
        ManagerFiller.scriptDir = scriptDir;
        ManagerFiller.modulesDir = modulesDir;
        readInModels(init, manager);
    }

    /**
     * read in modules from the defined HashMap from the initialisation.InitViaFile.init()
     * @param init read in init hashmap
     * @param manager
     */
    private static void readInModels(HashMap<String, HashMap<String, String>> init, ProcessManager manager) {
        init.keySet().forEach((x) -> {
            if (!x.equals("initValues")) {
                HashMap<String, String> moduleMap = trimKeys(init.get(x));
                SimpleProcessRunner spr = createSPRByMap(moduleMap);
                String group = getGroup(moduleMap);
                if (manager.getAllExistingGroups().contains(group)
                        &&
                        manager.getModulesOfGroup(group).stream()
                                .anyMatch(y ->
                                        y.getName().equals(spr.getName()) &&
                                        y.getType().equals(spr.getType()))
                ) {
                        manager.updateModule(group, spr.getName(), moduleMap);
                }
                manager.addModule(group, spr.getName(), spr);
            }
        });
    }

    /**
     * creating a Simple Process Runner by inserting a map with needed values
     * @param moduleMap module map
     * @return build SPR
     */
    @NotNull
    private static SimpleProcessRunner createSPRByMap(HashMap<String, String> moduleMap) {
        SimpleProcessRunner spr = null;
        try {
            spr = buildProcessRunner(moduleMap);
        } catch (ExecutableFileInRootDirectoryException | IOException e) {
            e.printStackTrace();
        }
        assert spr != null;
        return spr;
    }

    /**
     * returns the key value from the map with the key group
     * @param map map to read from
     * @return group value
     */
    private static String getGroup(HashMap<String, String> map) {
        if (!map.containsKey("group")) {
            return "default";
        }
        return map.get("group");
    }

    /**
     * build SimpleProcessRunner from given HashMap, which was read in from the initialisation.InitViaFile.init
     * @param map values map
     * @return SimpleProcessRunner build from the given HashMap
     * @throws ExecutableFileInRootDirectoryException is thrown, if the path to the given ProcessRunner file is null
     * @throws IOException is thrown, if any defined logger has problems with the given file path
     */
    private static SimpleProcessRunner buildProcessRunner(HashMap<String, String> map) throws ExecutableFileInRootDirectoryException, IOException {
        // read in module building needed variables
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
        if (type.equals("precompile")) {
            build = map.get("build");
            targetJar = map.get("target");
        }


        // build the actual ProcessRunner
        SimpleProcessRunner spr;
        switch (type) {
            case "precompile":
                spr = createPreCompilingSimpleProcessRunner(name, file, compiler, parameter, build, targetJar);
                break;
            case "single":
                spr = createSingleFileSimpleProcessRunner(name, file, interpreter, compiler, parameter);
                break;
            case "script":
                spr = createScriptFileSimpleProcessRunner(name, file, interpreter, parameter);
                break;
            default:
                throw new TypeNotPresentException("given ini type \"" + type + "\" for module named \"" + name +
                        "\" is not existent", new NullPointerException());
        }
        if (communication != null) {
            switch (communication) {
                case "socket":
                    return repackageProcessRunnerToSocketCommunicationProcessRunner(name, port, spr);
                case "none":
                    System.err.println("unnecessary communications token was detected and masterfully ignored");
                default:
                    System.err.println("communication type : " + communication + " not recognizable for module : " + name);
            }
        }
        return spr;
    }

    /**
     * repackaging any SimpleProcessRunner into a SocketCommunicationProcessRunner
     * @param name process name
     * @param port communication port
     * @param spr repackageable SPR
     * @return repackaged SocketCommunicationProcessRunner
     * @throws IOException is thrown, if the repackaging failed
     */
    private static SocketCommunicationProcessRunner repackageProcessRunnerToSocketCommunicationProcessRunner(String name, String port, SimpleProcessRunner spr) throws IOException {
        ServerSocket svso = null;
        try {
            assert port != null;
            svso = new ServerSocket(Integer.parseInt(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert spr != null;
        assert svso != null;
        return new SocketCommunicationProcessRunner(name, spr.getProcessBuilder(), loggerDir, svso);
    }

    /**
     * creating a script file SPR
     * @param name name of the process
     * @param file starting file
     * @param interpreter script interpreter
     * @param parameter parameter while starting
     * @return spr
     */
    private static SimpleProcessRunner createScriptFileSimpleProcessRunner(String name, String file, String interpreter, String parameter) {
        ArrayList<String> commandList;
        commandList = new ArrayList<>();
        commandList.add(interpreter);
        commandList.add(scriptDir + "/" + file);
        if (parameter != null) {
            Collections.addAll(commandList, parameter.split(" "));
        }
        SimpleProcessRunner spr = null;
        try {
            spr = new SimpleProcessRunner(name, ProcessRunnerType.SCRIPT_RUNNER, commandList, loggerDir) {
                @Override
                protected void afterStartProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process started");
                    }
                }

                @Override
                protected void afterStopProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process stopped");
                    }
                }

                @Override
                protected void afterRestartProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process restarted");
                    }
                }

                @Override
                protected void afterFinishProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process finished");
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spr;
    }

    /**
     * creating a single file SPR
     * @param name name of the process
     * @param file starting file
     * @param interpreter possible interpreter
     * @param compiler possible compiler
     * @param parameter parameter while starting
     * @return spr
     */
    private static SimpleProcessRunner createSingleFileSimpleProcessRunner(String name, String file, String interpreter, String compiler, String parameter) {
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
        SimpleProcessRunner spr = null;
        try {
            spr = new SimpleProcessRunner(name, pRType, commandList, loggerDir) {
                @Override
                protected void afterStartProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process started");
                    }
                }

                @Override
                protected void afterStopProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process stopped");
                    }
                }

                @Override
                protected void afterRestartProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process restarted");
                    }
                }

                @Override
                protected void afterFinishProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process finished");
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return spr;
    }

    /**
     * creating a pre-compiling SPR
     *
     * @param name name of process
     * @param file path to environment (directory)
     * @param compiler compiler for executable file
     * @param parameter parameter for execution start
     * @param build building executable line
     * @param target executable target
     * @return SPR
     * @throws ExecutableFileInRootDirectoryException if path does not exist
     * @throws IOException if file can't be accessed
     */
    private static SimpleProcessRunner createPreCompilingSimpleProcessRunner(String name, String file, String compiler, String parameter, String build, String target) throws ExecutableFileInRootDirectoryException, IOException {
        ScriptCreator buildScript = new ScriptCreator("bash", new File(scriptDir.getPath() + "/" + name + "_builder.sh"), loggerDir) {
            @Override
            public void afterRun(Process process) {
                log(LogType.INFO, "run successful");
            }
        };
        buildScript.addLineToScript("cd " + modulesDir + "/" + file + " || exit");
        buildScript.addLineToScript(build);
        if (parameter != null)
            buildScript.addLineToScript(compiler + " " + target + " " + parameter);
        else
            buildScript.addLineToScript(compiler + " " + target);
        SimpleProcessRunner spr = null;
        try {
            spr = new SimpleProcessRunner(name, ProcessRunnerType.PRECOMPILE_RUNNER, buildScript.buildRunnableProcessBuilder(), loggerDir) {
                @Override
                protected void afterStartProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process started");
                    }
                }

                @Override
                protected void afterStopProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process stopped");
                    }
                }

                @Override
                protected void afterRestartProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process restarted");
                    }
                }

                @Override
                protected void afterFinishProcessEvent() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(this.getProcessInputStream()));
                        BufferedReader be = new BufferedReader(new InputStreamReader(this.getProcessErrorStream()));
                        while (br.ready()) {
                            log(LogType.DEBUG, br.readLine());
                        }
                        while (br.ready()) {
                            log(LogType.DEBUG, be.readLine());
                        }
                    } catch (IOException e) {
                        log(LogType.ERROR, "failed to finish reading output/error from " + this.getName());
                    } finally {
                        log(LogType.INFO, "process finished");
                    }
                }
            };
        } catch (InterpreterOrScriptNotDefinedException | IOException e) {
            // TODO : add logger here
            e.printStackTrace();
        }
        return spr;
    }

    /**
     * method trims keys of HashMap
     * @param map map to trim
     * @return map trimmed
     */
    public static HashMap<String, String> trimKeys(HashMap<String, String> map) {
        HashMap<String, String> trimmedMap = new HashMap<>();
        for (String key : map.keySet()) {
            trimmedMap.put(key.trim(), map.get(key));
        }
        return trimmedMap;
    }
}

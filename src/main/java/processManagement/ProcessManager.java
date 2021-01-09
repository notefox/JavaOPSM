package processManagement;

import logger.LogType;
import logger.LoggableObject;
import org.jetbrains.annotations.NotNull;
import processManagement.processExceptions.ProcessAlreadyStartedException;
import processManagement.processExceptions.ProcessCouldNotStartException;
import processManagement.processExceptions.ProcessCouldNotStopException;
import processManagement.processExceptions.ProcessIsNotAliveException;
import processManagement.processRunner.constructObject.ProcessRunnerType;
import processManagement.processRunner.constructObject.SimpleProcessRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProcessManager extends LoggableObject {

    private final HashMap<String, HashMap<String, SimpleProcessRunner>> processesInGroups = new HashMap<>();

    public ProcessManager(File logDirectory) throws IOException {
        super("Main Process Manager", "ProcessManager_", logDirectory, 512, true);
    }

    public void addModule(@NotNull String group, @NotNull String name, @NotNull SimpleProcessRunner pr) {
        if (!processesInGroups.containsKey(group)) {
            processesInGroups.put(group, new HashMap<>());
        }
        processesInGroups.get(group).put(name, pr);
        log(LogType.INFO, "new process added : " + name + " in group : " + group + " | alive : " + pr.isProcessAlive());
    }

    public void addModule(@NotNull String name, @NotNull SimpleProcessRunner pr) { addModule("default", name, pr); }

    public SimpleProcessRunner getModuleOfGroupWithName(String group, String name) {
        return processesInGroups.get(group).get(name);
    }

    public List<SimpleProcessRunner> getModulesOfName(String name) {
        ArrayList<SimpleProcessRunner> listOfRunners = new ArrayList<>();
        processesInGroups.keySet().forEach(x -> {
            if (processesInGroups.get(x).containsKey(name)) {
                listOfRunners.add(processesInGroups.get(x).get(name));
            }
        });
        return listOfRunners;
    }

    public List<SimpleProcessRunner> getModulesOfGroup(String group) {
        ArrayList<SimpleProcessRunner> returner = new ArrayList<>();
        processesInGroups.get(group).keySet().stream().map(processesInGroups.get(group)::get).forEach(returner::add);
        return returner;
    }

    public List<SimpleProcessRunner> getAllModules() {
        ArrayList<SimpleProcessRunner> returner = new ArrayList<>();
        processesInGroups.keySet().
                forEach(x -> processesInGroups.get(x).keySet()
                        .stream()
                        .map(processesInGroups.get(x)::get)
                        .forEach(returner::add));
        return returner;
    }

    public void stopAll() {
        log(LogType.INFO, "stopping all running processes started");
        getAllModules().forEach(module -> {
            try {
                if (module.isProcessAlive()) {
                    module.stopProcess();
                    log(LogType.INFO, module + " was stopped");
                }
            } catch (InterruptedException e) {
                log(LogType.ERROR, "(manager) I was interrupted while stopping | please check manually if this " +
                        "manager is still running / existing");
            } catch (ProcessIsNotAliveException e) {
                log(LogType.WARNING, module.getName() + " : process which wasn't alive was tried to be stopped | " +
                        "is probably stopped now");
            } catch (ProcessCouldNotStopException e) {
                log(LogType.ERROR, module.getName() + " : process couldn't be stopped");
            }
        });
        log(LogType.INFO, "all running processes were stopped");
    }

    public void runAllScripts() {
        log(LogType.INFO, "starting all known scripts");
        getAllModules().forEach(module -> {
            if (!module.isProcessAlive() && module.getType() == ProcessRunnerType.SCRIPT_RUNNER) {
                try {
                    module.startProcess();
                } catch (IOException e) {
                    log(LogType.ERROR, module.getName() + " hasn't been able to be started | " + e.getMessage());
                } catch (ProcessAlreadyStartedException e) {
                    log(LogType.WARNING, module.getName() + " has been started running while trying to be started | " + e.getMessage());
                } catch (ProcessCouldNotStartException e) {
                    log(LogType.ERROR, module.getName() + " refused to start | " + e.getMessage());
                }
                log(LogType.INFO, module.getName() + " was started");
            }
        });
    }

    public void runAll() {
        log(LogType.INFO, "starting all known scripts and modules");
        getAllModules().forEach(module -> {
            try {
                module.startProcess();
            } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
                log(LogType.ERROR, module.getName() + " | " + e.getMessage());
            }
        });
    }

    public void runModule(String group, String processName) {
        try {
            processesInGroups.get(group).get(processName).startProcess();
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
            e.printStackTrace();
        }
    }

    public void waitForAll() throws InterruptedException {
        for (SimpleProcessRunner pr : getAllModules()) {
            pr.waitForProcess();
        }
    }

    public void waitForAll(long i) {
        ArrayList<Thread> waitingList = new ArrayList<>();
        for (SimpleProcessRunner pr : getAllModules()) {
            Thread t = new Thread(() -> {
                try {
                    pr.waitForProcess(i);
                } catch (InterruptedException | ProcessIsNotAliveException | ProcessCouldNotStopException e) {
                    e.printStackTrace();
                }
            });
            t.start();
            waitingList.add(t);
        }
        waitingList.forEach(x -> {
            try {
                x.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public List<SimpleProcessRunner> getModulesOfType(ProcessRunnerType type) {
        ArrayList<SimpleProcessRunner> returner = new ArrayList<>();
        getAllModules().stream().filter(x -> x.getType().equals(type)).forEach(returner::add);
        return returner;
    }

    public void runAllModulesOfType(ProcessRunnerType type) {
        getModulesOfType(type).forEach((x) -> {
            try {
                x.startProcess();
            } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void runAllModulesOfName(String name) {
        getModulesOfName(name).forEach(x -> {
            try {
                x.startProcess();
            } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void runAllModulesOfGroup(String group) {
        getModulesOfGroup(group).forEach(x -> {
            try {
                x.startProcess();
            } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void startSpecificModule(String group, String name) {
        try {
            getModuleOfGroupWithName(group, name).startProcess();
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
            System.err.println(e.getMessage());
        }
    }

    public void stopAllModulesWithType(ProcessRunnerType type) {
        getModulesOfType(type).stream().filter(SimpleProcessRunner::isProcessAlive).forEach(x -> {
            try {
                x.stopProcess();
            } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void stopAllModulesOfGroup(String group) {
        getModulesOfGroup(group).stream().filter(SimpleProcessRunner::isProcessAlive).forEach( x -> {
            try {
                x.stopProcess();
            } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void stopSpecificModule(String group, String name) {
        try {
            getModuleOfGroupWithName(group, name).stopProcess();
        } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    public void restartAll() {
        getAllModules().stream().filter(SimpleProcessRunner::isProcessAlive).forEach(x -> {
            try {
                x.restartProcess();
            } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void restartAllModulesOfType(ProcessRunnerType type) {
        getModulesOfType(type).stream().filter(SimpleProcessRunner::isProcessAlive).forEach(x -> {
            try {
                x.restartProcess();
            } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void restartAllModulesOfGroup(String group) {
        getModulesOfGroup(group).stream().filter(SimpleProcessRunner::isProcessAlive).forEach(x -> {
            try {
                x.restartProcess();
            } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | InterruptedException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    public void restartSpecificModule(String group, String name) {
        try {
            getModuleOfGroupWithName(group, name).restartProcess();
        } catch (ProcessIsNotAliveException | ProcessCouldNotStopException | IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("number of existing Processes : " + getAllModules().size() + "\n");
        sb.append("existing groups : \n");
        getAllExistingGroups().forEach(x -> {
            sb.append(" - " + x + " | nbr of Modules -> " + getModulesOfGroup(x).size() + "\n");
        });
        return sb.toString();
    }

    public List<String> getAllExistingGroups() {
        return new ArrayList<>(processesInGroups.keySet());
    }

    public void updateModule(String group, String name, HashMap<String, String> updatedMap) {
        getModuleOfGroupWithName(group, name).update(updatedMap);
    }
}

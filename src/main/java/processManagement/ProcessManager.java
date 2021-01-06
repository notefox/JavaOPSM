package processManagement;

import Logger.LogType;
import Logger.LoggableObject;
import org.jetbrains.annotations.NotNull;
import processManagement.ProcessExceptions.ProcessAlreadyStartedException;
import processManagement.ProcessExceptions.ProcessCouldNotStartException;
import processManagement.ProcessExceptions.ProcessCouldNotStopException;
import processManagement.ProcessExceptions.ProcessIsNotAliveException;
import processManagement.ProcessRunner.ProcessRunner;
import processManagement.ProcessRunner.ProcessRunnerType;
import processManagement.ProcessRunner.SimpleProcessRunner;

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

    public SimpleProcessRunner getModuleOfNameInGroup(String group, String name) {
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

    public List<SimpleProcessRunner> getAllModulesOfGroup(String group) {
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

    public void stopAllRunningProcesses() {
        log(LogType.INFO, "stopping all running processes started");
        getAllModules().forEach(module -> {
            try {
                if (module.isProcessAlive()) {
                    module.stopProcess();
                }
                log(LogType.INFO, module + " was stopped");
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
}

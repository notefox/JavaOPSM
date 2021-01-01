package ipcOverSockets;

import Logger.*;
import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStopException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.ProcessRunnerType;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProcessManager extends LoggableObject {

    private final HashMap<String, SimpleProcessRunner> processes = new HashMap<>();

    public ProcessManager(File logDirectory) throws IOException {
        super("Main Process Manager", "ProcessManager_", logDirectory, 512, true);
    }

    public void addModule(@NotNull String name, @NotNull SimpleProcessRunner pr) {
        processes.put(name, pr);
        log(LogType.INFO, "new process added : " + name + " | alive : " + pr.isProcessAlive());
    }

    public SimpleProcessRunner getModuleOfName(String name) {
        return processes.get(name);
    }

    public List<SimpleProcessRunner> getAllModules() {
        ArrayList<SimpleProcessRunner> returner = new ArrayList<>();
        processes.keySet().stream().map(processes::get).forEach(returner::add);
        return returner;
    }

    public void stopAllRunningProcesses() {
        log(LogType.INFO, "stopping all running processes started");
        processes.keySet().forEach(x -> {
            try {
                SimpleProcessRunner spr = processes.get(x);
                if (spr.isProcessAlive()) {
                    spr.stopProcess();
                }
                log(LogType.INFO, x + " was stopped");
            } catch (InterruptedException e) {
                log(LogType.ERROR, "(manager) I was interrupted while stopping | please check manually if this " +
                        "manager is still running / existing");
            } catch (ProcessIsNotAliveException e) {
                log(LogType.WARNING, x + " : process which wasn't alive was tried to be stopped | " +
                        "is probably stopped now");
            } catch (ProcessCouldNotStopException e) {
                log(LogType.ERROR, x + " : process couldn't be stopped");
            }
        });
        log(LogType.INFO, "all running processes were stopped");
    }

    public void runAllScripts() {
        log(LogType.INFO, "starting all known scripts");
        processes.keySet().forEach(x -> {
            SimpleProcessRunner spr = processes.get(x);
            if (!spr.isProcessAlive() && spr.getType() == ProcessRunnerType.SCRIPT_RUNNER) {
                try {
                    spr.startProcessWithoutRunningStartTest();
                } catch (IOException e) {
                    log(LogType.ERROR, x + " hasn't been able to be started");
                } catch (ProcessAlreadyStartedException e) {
                    log(LogType.WARNING, x + " has been started running while trying to be started");
                }
                log(LogType.INFO, x + " was started");
            }
        });
    }
}

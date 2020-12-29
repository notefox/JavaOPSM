package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.ProcessRunner;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ProcessManager {

    private final HashMap<String, SimpleProcessRunner> processes = new HashMap<>();

    public void addModule(@NotNull String name, @NotNull SimpleProcessRunner pr) {
        processes.put(name, pr);
    }


    public List<SimpleProcessRunner> getAllModules() {
        ArrayList<SimpleProcessRunner> returner = new ArrayList<>();
        processes.keySet().stream().map(processes::get).forEach(returner::add);
        return returner;
    }
}

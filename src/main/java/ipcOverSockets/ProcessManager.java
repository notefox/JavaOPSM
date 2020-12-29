package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.ProcessRunner;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ProcessManager {

    private HashMap<Long, ProcessRunner> processes = new HashMap<>();

    public void addModule(ProcessRunner pr) throws ProcessIsNotAliveException {
        processes.put(pr.getPID(), pr);
    }

    public List<ProcessRunner> getAllModules() {
        ArrayList<ProcessRunner> returner = new ArrayList<>();
        processes.keySet().stream().map((x) -> processes.get(x)).forEach(returner::add);
        return returner;
    }
}

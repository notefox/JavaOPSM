package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStopException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * standard process with communication over given port
 */
public class StandardProcess {

    private final ProcessBuilder pb;

    private Process process;

    private DataInputStream dis;
    private DataOutputStream dos;

    private ServerSocket seso;
    private Socket com;

    /**
     * Constructor for StandardProcess
     * @param command list of string which should start the process
     * @param seso ServerSocket for communication
     */
    public StandardProcess(ServerSocket seso, List<String> command) throws IOException {
       this.pb = new ProcessBuilder();
       this.pb.command(command);
       this.seso = seso;
    }

    /**
     * Process started
     * @throws IOException IOException may be thrown if being asked if alive
     * @throws ProcessCouldNotStartException is thrown, if the process wasn't alive after start
     * @throws ProcessAlreadyStartedException is thrown, if the process was already alive and needs to be stopped first
     */
    public void startProcess() throws IOException, ProcessCouldNotStartException, ProcessAlreadyStartedException {
        if (process != null && process.isAlive()) {
            throw new ProcessAlreadyStartedException("process already running, needs to be stopped first before start " +
                    "or if you want to restart, use the restart Method");
        }
        process = pb.start();
        if (!process.isAlive()) {
            throw new ProcessCouldNotStartException();
        }
        createConnectionToProcessOverSocket();
    }

    /**
     * Process stopper
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive in the first place
     * @throws ProcessCouldNotStopException is thrown, if the process did not stop after destroying
     */
    public void stopProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException {
        if (!process.isAlive()) {
            throw new ProcessIsNotAliveException();
        }
        process.destroy();
        if (process.isAlive()) {
            throw new ProcessCouldNotStopException();
        }
    }

    /**
     * Process Restarted
     *
     * simply calls stopProcess and startProcess after one another
     *
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive in the first place
     * @throws ProcessCouldNotStopException is thrown, if the process wasn't able to be stopped
     * @throws IOException can be thrown, when asking the process if it's alive
     * @throws ProcessCouldNotStartException is thrown, if the process wasn't able to start up again
     * @throws ProcessAlreadyStartedException is thrown if the process was already started by another source while restarting
     */
    public void restartProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, IOException, ProcessCouldNotStartException, ProcessAlreadyStartedException {
        stopProcess();
        startProcess();
    }

    /**
     * returns a DataInputStream for reading possible communication
     * @return DataInputStream from Process
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive
     */
    public DataInputStream getDis() throws ProcessIsNotAliveException {
        if (process == null || !process.isAlive())
            throw new ProcessIsNotAliveException();

        return dis;
    }

    /**
     * returns a DataOutputStream for writing to Process
     * @return DataOutputStream
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive
     */
    public DataOutputStream getDos() throws ProcessIsNotAliveException {
        if (process == null || !process.isAlive())
            throw new ProcessIsNotAliveException();

        return dos;
    }

    /**
     * returns a boolean for the process is alive call
     * (also returns false, even if Process is not defined yet)
     * @return boolean
     */
    public boolean isProcessAlive() {
        if (process == null) {
            return false;
        }
        return process.isAlive();
    }

    /**
     * connection creator, needs to be called every time the process is started/restarted
     * @throws IOException is thrown, if the connection was already existing, the connection timed out or the
     * input/outStreams were corrupted or not accessible
     */
    private void createConnectionToProcessOverSocket() throws IOException {
        if (seso == null || (com != null && com.isConnected())) {
            throw new ConnectException("connection already ongoing");
        }

        com = seso.accept();
        this.dos = new DataOutputStream(com.getOutputStream());
        this.dis = new DataInputStream(com.getInputStream());
    }
}

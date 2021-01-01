package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * standard process with communication over given port
 */
public class SocketCommunicationProcessRunner extends SimpleProcessRunner {

    private DataInputStream dis;
    private DataOutputStream dos;

    private final ServerSocket seso;
    private Socket com;

    /**
     * constructor for command list
     * @param name process name
     * @param seso server socket base for communication
     * @param logDir loggerDir
     * @param command command list
     * @throws IOException if thrown, if the logger has problems with the given path
     */
    public SocketCommunicationProcessRunner(@NotNull String name, @NotNull ServerSocket seso, File logDir, @NotNull List<String> command) throws IOException {
        super(name, ProcessRunnerType.SOCKET_COMMUNICATION_RUNNER, command, logDir);
        this.seso = seso;
    }

    /**
     * constructor for single command
     * @param name process name
     * @param processCommand executable command
     * @param logDir loggerDir
     * @param seso server socket base for communication
     * @throws IOException if thrown, if the logger has problems with the given path
     */
    public SocketCommunicationProcessRunner(@NotNull String name, @NotNull String processCommand, File logDir, @NotNull ServerSocket seso) throws IOException {
        super(name, ProcessRunnerType.SOCKET_COMMUNICATION_RUNNER, logDir, processCommand);
        this.seso = seso;
    }

    /**
     * constructor for ProcessBuilder
     * @param name name
     * @param pb ProcessBuilder
     * @param logDir loggerDir
     * @param seso ServerSocket for Communication basis
     * @throws IOException if thrown, if the logger has problems with the given path
     */
    public SocketCommunicationProcessRunner(@NotNull String name, @NotNull ProcessBuilder pb, File logDir, @NotNull ServerSocket seso) throws IOException {
        super(name, ProcessRunnerType.SOCKET_COMMUNICATION_RUNNER, pb, logDir);
        this.seso = seso;
    }

    @Override
    protected void afterStartProcessEvent() {
        try {
            createConnectionToProcessOverSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void afterStopProcessEvent() { }

    @Override
    protected void afterRestartProcessEvent() { }

    @Override
    protected void afterFinishProcessEvent() { }

    /**
     * returns a DataInputStream for reading possible communication
     * @return DataInputStream from Process
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive
     */
    public DataInputStream getDis() throws ProcessIsNotAliveException {
        if (!isProcessAlive())
            throw new ProcessIsNotAliveException();

        return dis;
    }

    /**
     * returns a DataOutputStream for writing to Process
     * @return DataOutputStream
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive
     */
    public DataOutputStream getDos() throws ProcessIsNotAliveException {
        if (isProcessAlive())
            throw new ProcessIsNotAliveException();

        return dos;
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

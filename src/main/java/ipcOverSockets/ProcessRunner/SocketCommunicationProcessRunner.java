package ipcOverSockets.ProcessRunner;

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
public class SocketCommunicationProcessRunner extends SimpleProcessRunner {

    private DataInputStream dis;
    private DataOutputStream dos;

    private final ServerSocket seso;
    private Socket com;

    /**
     * Constructor for StandardProcess
     * @param command list of string which should start the process
     * @param seso ServerSocket for communication
     */
    public SocketCommunicationProcessRunner(String name, ServerSocket seso, List<String> command) {
        super(name, command);
        this.seso = seso;
    }

    /**
     * constructor for a single line command for a process with a server socket
     * @param processCommand command to execute
     * @param seso ServerSocket for communication
     */
    public SocketCommunicationProcessRunner(String name, String processCommand, ServerSocket seso) {
        super(name, processCommand);
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

package ipcOverSockets.ProcessRunnerTemplates;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.SocketCommunicationProcessRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class JavaSocketCommunicationTemplate {
    public static void main(String[] args) {
        ServerSocket server = null;
        int port = 9000;

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<String> commandList = new ArrayList<>();
            commandList.add("java");
            commandList.add("modules/SingleFile/SingleClassModuleExample.java");
            commandList.add("file=file");
            commandList.add("name=testProcess");
            commandList.add("port=" + port);
            SocketCommunicationProcessRunner pro = new SocketCommunicationProcessRunner(server, commandList);
            pro.startProcess();
            pro.getDos().writeUTF("hello");
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | ProcessIsNotAliveException e) {
            e.printStackTrace();
        }
    }
}

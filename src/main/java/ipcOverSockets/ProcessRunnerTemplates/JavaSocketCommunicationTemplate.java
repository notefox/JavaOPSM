package ipcOverSockets.ProcessRunnerTemplates;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;
import ipcOverSockets.ProcessRunner.SocketCommunicationProcessRunner;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

@SuppressWarnings("CommentedOutCode")
public class JavaSocketCommunicationTemplate {
    public static SocketCommunicationProcessRunner buildTemplate() {
        ServerSocket server = null;
        int port = 9000;

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SocketCommunicationProcessRunner pro = null;
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("java");
        commandList.add("modules/SingleFile/SingleClassModuleExample.java");
        commandList.add("file=file");
        commandList.add("name=testProcess");
        commandList.add("port=" + port);
        pro = new SocketCommunicationProcessRunner("java socket template", server, commandList);
        /*
         pro.startProcess();
         pro.getDos().writeUTF("hello");
        */
        return pro;
    }
}

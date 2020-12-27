package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainProcessSocket {

    private static ServerSocket server;
    private static int port = 9000;

    public static void main(String[] args) {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<String> list = new ArrayList<>();
            list.add("java");
            list.add("src/main/java/ipcOverSockets/TestIPCOverSockets.java");
            list.add("file=file");
            list.add("name=testProcess");
            list.add("port=" + port);
            StandardProcess pro = new StandardProcess(server,list);
            pro.startProcess();
            pro.getDos().writeUTF("hello");
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | ProcessIsNotAliveException e) {
            e.printStackTrace();
        }


    }
}

package ipcOverSockets;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestCommunicationServer {
    public static void main(String[] args) throws IOException {
        ServerSocket s = new ServerSocket(9000);
        Socket communication = s.accept();
        System.out.println(communication.getInetAddress());
        DataOutputStream dos = new DataOutputStream(communication.getOutputStream());
        dos.writeUTF("exit");
    }
}

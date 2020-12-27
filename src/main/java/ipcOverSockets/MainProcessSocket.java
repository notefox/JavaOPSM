package ipcOverSockets;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MainProcessSocket {
    public static void main(String[] args) {
        int port = 9000;
        Process p = createProcessForCommunication(port);
        DataOutputStream dos = getDISForCreatedProcess(port);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        giveInput(dos, "input");
        giveInput(dos, "exit");

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static DataOutputStream getDISForCreatedProcess(int port) {
        Socket comunication = null;
        try {
            ServerSocket s = new ServerSocket(port);
            comunication = s.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert comunication != null;
        DataOutputStream dos = null;

        try {
            dos = new DataOutputStream(comunication.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dos;
    }

    private static Process createProcessForCommunication(int port) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", "src/main/java/ipcOverSockets/TestIPCOverSockets.java", "file=file", "name=testProcess", "port=" + port);
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert p != null;
        return p;
    }

    private static void giveInput(DataOutputStream bw, String input) {
        try {
            bw.writeUTF(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

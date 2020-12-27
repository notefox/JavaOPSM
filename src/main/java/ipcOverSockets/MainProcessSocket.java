package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.JavaSocketCommunicationProcessRunner;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;

public class MainProcessSocket {

    public static void main(String[] args) {
        //exampleJavaModuleRun();
        examplePythonScriptRun();
    }

    private static void examplePythonScriptRun() {
        ArrayList<String> args = new ArrayList<>();
        args.add("python");
        args.add("modules/SingleFile/Python/singleFilePythonExample.py");
        args.add("someFile.txt");
        SimpleProcessRunner spr = new SimpleProcessRunner(args) {
            @Override
            protected void afterStartProcessEvent() {
            }

            @Override
            protected void afterStopProcessEvent() {
            }
        };
        try {
            spr.startProcess();
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException e) {
            e.printStackTrace();
        }
    }

    private static void exampleJavaModuleRun() {
        ServerSocket server = null;
        int port = 9000;

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<String> args = new ArrayList<>();
            args.add("java");
            args.add("modules/SingleFile/SingleClassModuleExample.java");
            args.add("file=file");
            args.add("name=testProcess");
            args.add("port=" + port);
            JavaSocketCommunicationProcessRunner pro = new JavaSocketCommunicationProcessRunner(server,args);
            pro.startProcess();
            pro.getDos().writeUTF("hello");
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | ProcessIsNotAliveException e) {
            e.printStackTrace();
        }
    }


}

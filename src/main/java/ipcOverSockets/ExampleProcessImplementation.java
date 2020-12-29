package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;
import ipcOverSockets.ProcessRunner.SocketCommunicationProcessRunner;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ExampleProcessImplementation {

    public static void main(String[] args) {
        //exampleJavaModuleRun();
        //examplePythonScriptRun();
        exampleMavenProjectModuleRun();
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

            @Override
            protected void afterRestartProcessEvent() {

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
            SocketCommunicationProcessRunner pro = new SocketCommunicationProcessRunner(server,args);
            pro.startProcess();
            pro.getDos().writeUTF("hello");
        } catch (IOException | ProcessCouldNotStartException | ProcessAlreadyStartedException | ProcessIsNotAliveException e) {
            e.printStackTrace();
        }
    }

    private static void exampleMavenProjectModuleRun() {
        String pathToMVNProject = "modules/Projects/mavenExampleProject";

        // create script file
        File scriptGeneration = new File("scripts");
        scriptGeneration.mkdir();
        File script = new File(scriptGeneration.getPath() + "/mvn_compile_script.sh");
        try { script.createNewFile(); } catch (IOException e) { e.printStackTrace(); }

        // create necessary script
        StringBuilder sb = new StringBuilder();
        sb.append("cd ").append(pathToMVNProject).append(" || exit\n");
        sb.append("mvn install\n");
        try {
            FileWriter fw = new FileWriter(script);
            fw.write(sb.toString());
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("bash");
        commandList.add(script.getPath());

        SimpleProcessRunner spr = new SimpleProcessRunner(commandList) {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {

            }
        };

        try {
            spr.startProcessWithoutRunningStartTest();
            spr.waitForProcess();
            InputStream is = spr.getProcessInputStream();
            InputStream es = spr.getProcessErrorStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            BufferedReader be = new BufferedReader(new InputStreamReader(es));

            while (br.ready()) {
                System.out.println(br.readLine());
            }

            while (be.ready()) {
                System.out.println(be.readLine());
            }

        } catch (IOException | ProcessAlreadyStartedException | ProcessIsNotAliveException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package ipcOverSockets;

import ipcOverSockets.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import ipcOverSockets.ProcessRunner.ScriptCreator;
import ipcOverSockets.ProcessRunner.SimpleProcessRunner;
import ipcOverSockets.ProcessRunner.SocketCommunicationProcessRunner;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;

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

    @SuppressWarnings("CommentedOutCode")
    private static void exampleMavenProjectModuleRun() {
        String pathToMVNProject = "modules/Projects/mavenExampleProject";

        // create script file
        File script = new File("scripts/mvn_compile_script.sh");
        ScriptCreator sc = new ScriptCreator("bash", script) {
            @Override
            public void afterRun(Process process) {
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader be = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                try {
                    while (br.ready()) {
                        System.out.println(br.readLine());
                    }
                    while (be.ready()) {
                        System.err.println(be.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // create necessary script
        sc.addLineToScript("cd " + pathToMVNProject + " || exit");
        sc.addLineToScript("mvn install\n");
        sc.addLineToScript("java -jar target/mavenExampleProject-0.1.jar");

        // sc.addLineToScript("pwd");

        // either run it directly and wait for it
        try {
            sc.runDirectly();
        } catch (IOException | InterpreterOrScriptNotDefinedException | InterruptedException e) {
            e.printStackTrace();
        }

        // or just start it and let it do it's job
        // sc.startDirectly();

        // or just build a SimpleProcessRunner out of it
        /*SimpleProcessRunner spr = null;
        try {
            spr = new SimpleProcessRunner(sc.buildRunnableProcessBuilder()) {
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
        } catch (IOException | InterpreterOrScriptNotDefinedException e) {
            e.printStackTrace();
        }
        try {
            assert spr != null;
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
            InputStream es = spr.getProcessErrorStream();
            BufferedReader be = new BufferedReader(new InputStreamReader(es));
            try {
                while (be.ready()) {
                    System.out.println(be.readLine());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }*/
    }
}

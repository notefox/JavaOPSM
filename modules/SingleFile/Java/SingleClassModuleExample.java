import java.io.*;
import java.net.Socket;


/**
 * example object for IPC Socket connection
 */
public class SingleClassModuleExample {
    private static String outputFile = "unidentifiedProcessOutputFile.txt";
    private static String processName = "unidentifiedProcess";

    private static int port = 9000;

    private static Socket communication;

    public static void main(String[] args) {
        initWithArgsIfArgsExist(args);
        rerouteSysOut();
        createFileIfNotExist(outputFile);

        runtime();

    }

    private static void initWithArgsIfArgsExist(String[] args) {
        if (args.length == 0) {
            //System.exit(1);
        } else {
            initializeWithArgs(args);
        }
    }

    private static void rerouteSysOut() {
        String sysOutNameFilename = processName + ".out";
        String sysErrNameFilename = processName + ".err";

        createFileIfNotExist(sysOutNameFilename);
        createFileIfNotExist(sysErrNameFilename);

        try {
            System.setOut(new PrintStream(new FileOutputStream(sysOutNameFilename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try{
            System.setErr(new PrintStream(new FileOutputStream(sysErrNameFilename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void createFileIfNotExist(String sysOutNameFilename) {
        if (!new File(sysOutNameFilename).exists()) {
            try {
                if (!new File(sysOutNameFilename).createNewFile()) {
                    System.exit(3);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void runtime() {
        try {
            communication = new Socket("localhost", port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataInputStream dis = null;

        try {
            dis = new DataInputStream(communication.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i = 0;
        assert dis != null;
        while (true) {
            try {
                String read = dis.readUTF();
                checkForKeyWord(read);
                System.out.println(i + "" + read);
                i++;
            } catch (EOFException e) {
                System.err.println("Socket Stream EOF , therefore stop this Module");
                System.exit(51);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void checkForKeyWord(String lineToPrint) {
        if (lineToPrint.equals("exit")) {
            System.exit(0);
        } else {
            System.err.println("no keyword found");
        }
    }

    private static void initializeWithArgs(String[] args) {
        for (String s: args) {
            String[] split = s.split("=");
            if (split.length != 2) {
                System.exit(2);
            }
            switch (split[0]) {
                case "file":
                    outputFile = split[1];
                    break;
                case "name":
                    processName = split[1];
                    break;
                case "port":
                    port = Integer.parseInt(split[1]);
                    break;
            }
        }
    }
}

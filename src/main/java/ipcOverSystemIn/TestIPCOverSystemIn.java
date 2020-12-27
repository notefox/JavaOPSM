package ipcOverSystemIn;

import java.io.*;

public class TestIPCOverSystemIn {
    public static void main(String[] args) throws FileNotFoundException {
        System.setOut(new PrintStream(new FileOutputStream("somefile.out")));
        DataInputStream dis = new DataInputStream(System.in);
        try {
            System.out.println(dis.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done");
        System.exit(0);
    }
}

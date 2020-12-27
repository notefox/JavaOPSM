package ipcOverSystemIn;

import java.io.DataOutputStream;
import java.io.IOException;

public class MainProcessSystemIn {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("java", "src/main/java/ipcOverSystemIn/TestIPCOverSystemIn.java");
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert p != null;
        DataOutputStream dos = new DataOutputStream(p.getOutputStream());
        dos.writeUTF("hello");
        System.out.println(p.isAlive());
        p.waitFor();
    }
}

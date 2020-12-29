import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File testFile = new File("testFile.txt");
        new FileWriter(testFile).write("test");
        System.exit(0);
    }
}

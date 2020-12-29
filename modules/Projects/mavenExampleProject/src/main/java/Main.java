import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        File testFile = new File("testFile.txt");
        FileWriter fw = new FileWriter(testFile);
        fw.write("hello world");
        fw.flush();
        fw.close();
        TestClass test = new TestClass();
        test.test();
        System.exit(0);
    }
}

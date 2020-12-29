import java.io.File;
import java.io.IOException;

public class TestContainer {
    public static void main(String[] args) {
        System.out.println(new File("modules/SingleFile/Python/singleFilePythonExample.py").getParent());
    }
}

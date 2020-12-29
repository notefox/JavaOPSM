import java.io.IOException;

public class TestContainer {
    public static void main(String[] args) {
        System.out.println(new ProcessBuilder().command("/bin/pwd").command());
    }
}

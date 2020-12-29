import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestContainer {
    public static void main(String[] args) throws IOException {
        Process p = new ProcessBuilder().command("sleep", "15").start();
        System.out.println(p.info());
        String[] arguments = new String[0];
        String command = null;
        if (p.info().command().isPresent() || p.info().arguments().isPresent()) {
            command = p.info().command().get();
            arguments = p.info().arguments().get();
        }
        ProcessBuilder reproduced = new ProcessBuilder();
        List<String> commandList = new ArrayList<>();
        commandList.add(command);
        commandList.addAll(Arrays.asList(arguments));
        reproduced.command(commandList);
        Process reproducedProcess = reproduced.start();
        System.out.println(reproducedProcess.info().commandLine().equals(p.info().commandLine()));
        System.out.println(reproducedProcess.info().commandLine());
    }
}

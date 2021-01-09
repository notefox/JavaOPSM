package processManagement.processRunnerTemplates;

import processManagement.processRunner.SocketCommunicationProcessRunner;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

@SuppressWarnings("CommentedOutCode")
public class JavaSocketCommunicationTemplate {
    public static SocketCommunicationProcessRunner buildTemplate() {
        ServerSocket server = null;
        int port = 9000;

        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SocketCommunicationProcessRunner pro = null;
        ArrayList<String> commandList = new ArrayList<>();
        commandList.add("java");
        commandList.add("modules/SingleFile/SingleClassModuleExample.java");
        commandList.add("file:file");
        commandList.add("name:testProcess");
        commandList.add("port:" + port);
        assert server != null;
        try {
            pro = new SocketCommunicationProcessRunner("java socket template", server,new File("logger"),  commandList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
         pro.startProcess();
         pro.getDos().writeUTF("hello");
        */
        return pro;
    }
}

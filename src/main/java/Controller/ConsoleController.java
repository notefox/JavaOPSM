package Controller;

import processManagement.ProcessManager;

import java.io.Console;

public class ConsoleController implements Runnable {

    private static ProcessManager manager;

    public static void giveManager(ProcessManager manager) {
        ConsoleController.manager = manager;
    }


    private void printStart() {
        System.out.println("-- started in Console Manager Mode --");
    }

    @Override
    public void run() {

    }
}

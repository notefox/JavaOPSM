import controller.ConsoleController;
import initialisation.InitViaFile;
import initialisation.ManagerFiller;

import processManagement.processExceptions.ProcessAlreadyStartedException;
import processManagement.processExceptions.ProcessCouldNotStartException;
import processManagement.ProcessManager;

import java.io.*;
import java.util.HashMap;

public class MainRuntime {

    /**
     * script path
     */
    private static File scriptDir;

    /**
     * modules path
     */
    private static File modulesDir;

    /**
     * logger path
     */
    private static File loggerDir;

    /**
     * used process manager
     */
    private static ProcessManager manager;

    /**
     * main executive method
     * @param args given arguments while execution
     */
    public static void main(String[] args) throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, InterruptedException {
        start();
    }

    private static void start() {
        try {
            initIntoManager();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConsoleController.giveManager(manager, System.in, System.out, System.err);
    }

    /**
     * initializer method
     * @throws IOException is thrown, if the ini file reading wasn't successful
     */
    private static void initIntoManager() throws IOException {
        HashMap<String, HashMap<String, String>> init = InitViaFile.init(new File("init.ini"));
        // ------------------ do not change this ------------------
        readInDefaultDir(init);
        if (manager == null) {
            try {
                initializeManager(loggerDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // --------------------------------------------------------
        ManagerFiller.fillManager(init, MainRuntime.manager, loggerDir, scriptDir, modulesDir);
    }

    /**
     * define default dirs
     * (like scriptDir, modulesDir or loggerDir)
     * @param init init file to read from
     */
    private static void readInDefaultDir(HashMap<String, HashMap<String, String>> init) {
        HashMap<String, String> defaultVariables = init.get("initValues");
        defaultVariables = ManagerFiller.trimKeys(defaultVariables);
        MainRuntime.modulesDir = new File(defaultVariables.get("modules_dir"));
        MainRuntime.scriptDir = new File(defaultVariables.get("scripts_dir"));
        MainRuntime.loggerDir = new File(defaultVariables.get("logger_dir"));
    }

    /**
     * initialize local process manager
     * @param loggerDir log directory for the given processes
     * @throws IOException is thrown, if any defined logger has problems with the given file path
     */
    private static void initializeManager(File loggerDir) throws IOException {
        MainRuntime.manager = new ProcessManager(new File(loggerDir.getPath() + "/processManager"));
    }

}

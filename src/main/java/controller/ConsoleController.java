package controller;

import processManagement.ProcessManager;
import processManagement.processRunner.constructObject.ProcessRunnerType;

import java.io.*;

public class ConsoleController implements Runnable {

    public static ConsoleController instance;

    private static ProcessManager manager;
    private static PrintStream output;
    private static PrintStream error;
    private static BufferedReader br;

    public static final String HELP_COMMAND = "help";
    public static final String INFO_COMMAND = "info";
    public static final String LIST_COMMAND = "list";
    public static final String RUN_COMMAND = "run";
    public static final String STOP_COMMAND = "stop";
    public static final String RESTART_COMMAND = "restart";

    public static void giveManager(ProcessManager manager, InputStream is, PrintStream os, PrintStream er) {
        br = new BufferedReader(new InputStreamReader(is));
        output = os;
        error = er;
        ConsoleController.manager = manager;
        instance = new ConsoleController();
        instance.run();
    }

    private void printStart() {
        System.out.println("-- started in Console Manager Mode --");
    }
    private void printAvailable() {
        output.println(INFO_COMMAND + " - information about this process manager");
        output.println(LIST_COMMAND + " | " + LIST_COMMAND + " <group> | " + LIST_COMMAND + " <type> - lists either all available processes, processes of a group or of a type ");
        output.println(RUN_COMMAND + " <group> | " + RUN_COMMAND + " <group> <name> | " + RUN_COMMAND + " <type> - runs all processes of a specific group, group and name or type");
        output.println(STOP_COMMAND + " <group> | " + STOP_COMMAND + " <group> <name> | " + STOP_COMMAND + " <type> - stops all processes of a specific group, group and name or type");
        output.println(RESTART_COMMAND + " <group> | " + RESTART_COMMAND + " <group> <name> | " + RESTART_COMMAND + " <type> - restarts all processes of a specific group, group and name or type");
        output.println("-------");
        output.println("available types: ");
        ProcessRunnerType.getAllTypes().stream().map(ProcessRunnerType::getValue).forEach(output::println);
    }

    @Override
    public void run() {
        printStart();
        while (true) {
            System.out.print(" > ");
            System.out.flush();
            try {
                String input = br.readLine();
                String[] inputSplit = input.split(" ");
                inputAnalysis(inputSplit);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void inputAnalysis(String[] inputSplit) {
        switch (inputSplit[0]) {
            case HELP_COMMAND:
                printAvailable();
                break;
            case INFO_COMMAND:
                printInfoCommand();
                break;
            case LIST_COMMAND:
                printListCommand(inputSplit);
                break;
            case RUN_COMMAND:
                runCommand(inputSplit);
                break;
            case STOP_COMMAND:
                stopCommand(inputSplit);
                break;
            case RESTART_COMMAND:
                restartCommand(inputSplit);
            default:
                error.println("unknown command " + inputSplit[0]);
        }
    }

    private void restartCommand(String[] inputSplit) {
        switch (inputSplit.length - 1) {
            case 0:
                manager.restartAll();
                break;
            case 1:
                // did the input match any existing Runner types
                if (ProcessRunnerType.getAllTypes().stream().map(ProcessRunnerType::getValue).anyMatch(x -> x.equals(inputSplit[1])))
                    manager.restartAllModulesOfType(ProcessRunnerType.parseValue(inputSplit[1]));
                else if (manager.getAllExistingGroups().contains(inputSplit[1]))
                    manager.restartAllModulesOfGroup(inputSplit[1]);
                else
                    error.println("found nothing to restart");
                break;

            case 2:
                manager.restartSpecificModule(inputSplit[1], inputSplit[2]);
            default:
                error.println("too many arguments for restart");
        }
    }

    private void stopCommand(String[] inputSplit) {
        switch (inputSplit.length - 1) {
            case 0:
                manager.stopAll();
                break;
            case 1:
                // did the input match any existing Runner types
                if (ProcessRunnerType.getAllTypes().stream().map(ProcessRunnerType::getValue).anyMatch(x -> x.equals(inputSplit[1])))
                    manager.stopAllModulesWithType(ProcessRunnerType.parseValue(inputSplit[1]));
                else if (manager.getAllExistingGroups().contains(inputSplit[1]))
                    manager.stopAllModulesOfGroup(inputSplit[1]);
                else
                    error.println("found nothing to stop");
                break;

            case 2:
                manager.stopSpecificModule(inputSplit[1], inputSplit[2]);
            default:
                error.println("too many arguments for stop");
        }
    }

    private void runCommand(String[] inputSplit) {
        switch (inputSplit.length - 1) {
            case 0:
                manager.runAll();
                break;
            case 1:
                // did the input match any existing Runner types
                if (ProcessRunnerType.getAllTypes().stream().map(ProcessRunnerType::getValue).anyMatch(x -> x.equals(inputSplit[1])))
                    manager.runAllModulesOfType(ProcessRunnerType.parseValue(inputSplit[1]));
                else if (manager.getAllExistingGroups().contains(inputSplit[1]))
                    manager.runAllModulesOfGroup(inputSplit[1]);
                else
                    error.println("found nothing to run");
                break;

            case 2:
                manager.startSpecificModule(inputSplit[1], inputSplit[2]);
            default:
                error.println("too many arguments for run");
        }
    }

    private void printListCommand(String[] inputSplit) {
        switch (inputSplit.length - 1) {
            case 0:
                manager.getAllModules().forEach(x -> output.println(x.toString()));
                break;
            case 1:
                // did the input match any existing Runner types
                if (ProcessRunnerType.getAllTypes().stream().map(ProcessRunnerType::getValue).anyMatch(x -> x.equals(inputSplit[1])))
                    manager.getModulesOfType(ProcessRunnerType.parseValue(inputSplit[1])).forEach(System.out::println);
                else if (manager.getAllExistingGroups().contains(inputSplit[1]))
                    manager.getModulesOfGroup(inputSplit[1]).forEach(System.out::println);
                else
                    error.println("found nothing to list");
                break;
            default:
                error.println("too many arguments for list");
        }
    }

    private void printInfoCommand() {
        output.println(manager.toString());
    }
}

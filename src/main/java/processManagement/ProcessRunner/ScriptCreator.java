package processManagement.ProcessRunner;

import Logger.LogType;
import Logger.LoggableObject;
import processManagement.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import processManagement.ProcessExceptions.ExecutableFileInRootDirectoryException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;


/**
 * script builder class
 */
public abstract class ScriptCreator extends LoggableObject {

    private final String interpreter;
    private final File scriptPath;
    private final File script;
    private final LinkedList<String> scriptLines = new LinkedList<>();

    /**
     * constructor for script builder
     * @param interpreter script interpreter path
     * @param script script file
     */
    public ScriptCreator(String interpreter, File script, File logDir) throws ExecutableFileInRootDirectoryException, IOException {
        super(script.getName(), "ScriptCreator_", new File(logDir.getPath() + "/" + script.getName()),
                512, true);
        this.interpreter = interpreter;
        try {
            this.scriptPath = new File(script.getParent());
        } catch (NullPointerException e) {
            throw new ExecutableFileInRootDirectoryException("script is not allowed to be in the root directory", e);
        }
        this.script = script;
    }

    /**
     * adder method to script
     * @param line adds script line
     */
    public void addLineToScript(String line) {
        log(LogType.INFO, "added line : " + line);
        scriptLines.add(line);
    }

    /**
     * returns path to script
     * @return String
     */
    public String getScriptPath() {
        return script.getPath();
    }

    /**
     * builds a ProcessRunner for execution of the Script for re-usability
     * @return ProcessRunner
     * @throws IOException is thrown, if the Script couldn't be build ( writing to the script failed )
     * @throws InterpreterOrScriptNotDefinedException is thrown, if the Interpreter or Script weren't given correctly
     * (meaning, they were null or empty)
     * (since both interpreter variable and script variable are final in here, this ScriptGenerator isn't really usable)
     */
    public ProcessBuilder buildRunnableProcessBuilder() throws IOException, InterpreterOrScriptNotDefinedException {
        log(LogType.INFO, "building runnable process builder");
        // null-save String.equals()
        if((interpreter == null || scriptPath == null) || (Objects.equals(interpreter, "") || Objects.equals(scriptPath.getPath(), ""))) {
            log(LogType.ERROR, "interpreter or scriptPath wasn't defined | interpreter : " + interpreter + " | Path : " + script.getPath());
            throw new InterpreterOrScriptNotDefinedException();
        }
        buildScript();
        log(LogType.INFO, "script building was successful");
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(interpreter, script.getPath());
        return pb;
    }

    /**
     * builds the script and runs it directly out of the box with a wait for at the end
     * @throws IOException is thrown, if the Script couldn't be build ( writing to the script failed )
     * @throws InterpreterOrScriptNotDefinedException is thrown, if the Interpreter or Script weren't given correctly
     * (meaning, they were null or empty)
     * (since both interpreter variable and script variable are final in here, this ScriptGenerator isn't really usable)
     * @throws InterruptedException is thrown, if "wait for script" was interrupted
     * @return ran Process
     */
    public Process runDirectly() throws IOException, InterpreterOrScriptNotDefinedException, InterruptedException {
        log(LogType.INFO, "running script directly");
        // null-save String.equals()
        if((interpreter == null || scriptPath == null) || (Objects.equals(interpreter, "") || Objects.equals(scriptPath.getPath(), ""))) {
            log(LogType.ERROR, "interpreter or scriptPath wasn't defined | interpreter : " + interpreter + " | Path : " + script.getPath());
            throw new InterpreterOrScriptNotDefinedException();
        }
        buildScript();
        log(LogType.INFO, "script building was successful");
        Process process = buildRunnableProcessBuilder().start();
        process.waitFor();
        afterRun(process);
        return process;
    }

    /**
     * builds the script and runs it directly out of the box without waiting for it to end
     * @throws IOException is thrown, if the Script couldn't be build ( writing to the script failed )
     * @throws InterpreterOrScriptNotDefinedException is thrown, if the Interpreter or Script weren't given correctly
     * (meaning, they were null or empty)
     * (since both interpreter variable and script variable are final in here, this ScriptGenerator isn't really usable)
     * @return now running Process
     */
    public Process startDirectly() throws IOException, InterpreterOrScriptNotDefinedException {
        log(LogType.INFO, "starting script directly");
        // null-save String.equals()
        if((interpreter == null || scriptPath == null) || (Objects.equals(interpreter, "") || Objects.equals(scriptPath.getPath(), ""))) {
            log(LogType.ERROR, "interpreter or scriptPath wasn't defined | interpreter : " + interpreter + " | Path : " + script.getPath());
            throw new InterpreterOrScriptNotDefinedException();
        }
        buildScript();
        log(LogType.INFO, "building script was successful");
        return buildRunnableProcessBuilder().start();
    }

    /**
     * event method called after run call
     * @param process executed process
     */
    public abstract void afterRun(Process process);

    /**
     * script builder
     * @throws IOException is thrown, if writing to the script failed
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void buildScript() throws IOException {
        scriptPath.mkdir();
        script.createNewFile();

        FileWriter fw = new FileWriter(script);
        StringBuilder sb = new StringBuilder();
        scriptLines.forEach(x -> sb.append(x).append("\n"));
        fw.write(sb.toString());
        fw.flush();
        fw.close();
    }

    @Override
    public String toString() {
        return "ScriptCreator{" +
                "interpreter='" + interpreter + '\'' +
                ", scriptPath=" + scriptPath +
                ", script=" + script +
                ", scriptLines=" + scriptLines +
                '}';
    }
}

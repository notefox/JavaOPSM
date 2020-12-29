package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.InterpreterOrScriptNotDefinedException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;


/**
 * script builder class
 */
public abstract class ScriptCreator {

    private final String interpreter;
    private final File scriptPath;
    private final File script;
    private final LinkedList<String> scriptLines = new LinkedList<>();

    /**
     * constructor for script builder
     * @param interpreter script interpreter path
     * @param script script file
     */
    public ScriptCreator(String interpreter, File script) {
        this.interpreter = interpreter;
        this.scriptPath = new File(script.getParent());
        this.script = script;
    }

    /**
     * adder method to script
     * @param line adds script line
     */
    public void addLineToScript(String line) {
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
        // null-save String.equals()
        if((interpreter == null || scriptPath == null) || (Objects.equals(interpreter, "") || Objects.equals(scriptPath.getPath(), ""))) {
            throw new InterpreterOrScriptNotDefinedException();
        }
        buildScript();
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
     */
    public void runDirectly() throws IOException, InterpreterOrScriptNotDefinedException, InterruptedException {
        // null-save String.equals()
        if((interpreter == null || scriptPath == null) || (Objects.equals(interpreter, "") || Objects.equals(scriptPath.getPath(), ""))) {
            throw new InterpreterOrScriptNotDefinedException();
        }
        buildScript();
        Process process = buildRunnableProcessBuilder().start();
        process.waitFor();
        afterRun(process);
    }

    /**
     * builds the script and runs it directly out of the box without waiting for it to end
     * @throws IOException is thrown, if the Script couldn't be build ( writing to the script failed )
     * @throws InterpreterOrScriptNotDefinedException is thrown, if the Interpreter or Script weren't given correctly
     * (meaning, they were null or empty)
     * (since both interpreter variable and script variable are final in here, this ScriptGenerator isn't really usable)
     */
    public void startDirectly() throws IOException, InterpreterOrScriptNotDefinedException {
        // null-save String.equals()
        if((interpreter == null || scriptPath == null) || (Objects.equals(interpreter, "") || Objects.equals(scriptPath.getPath(), ""))) {
            throw new InterpreterOrScriptNotDefinedException();
        }
        buildScript();
        buildRunnableProcessBuilder().start();
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
}

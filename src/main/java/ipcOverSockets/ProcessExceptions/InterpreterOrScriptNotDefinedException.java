package ipcOverSockets.ProcessExceptions;

public class InterpreterOrScriptNotDefinedException extends Throwable {
    public InterpreterOrScriptNotDefinedException() {
        super("no interpreter or script was given or it was empty");
    }

    public InterpreterOrScriptNotDefinedException(String message) {
        super(message);
    }

    public InterpreterOrScriptNotDefinedException(Throwable cause) {
        super(cause);
    }
}

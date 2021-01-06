package processManagement.ProcessExceptions;

public class ProcessNotExitedYetException extends Exception {
    public ProcessNotExitedYetException() {
        super("process is still alive");
    }

    public ProcessNotExitedYetException(String message) {
        super(message);
    }

    public ProcessNotExitedYetException(Throwable cause) {
        super(cause);
    }
}

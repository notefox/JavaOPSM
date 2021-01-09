package processManagement.processExceptions;

public class ProcessAlreadyStartedException extends Throwable {
    public ProcessAlreadyStartedException() {
        super("process is already alive");
    }

    public ProcessAlreadyStartedException(String message) {
        super(message);
    }

    public ProcessAlreadyStartedException(Throwable cause) {
        super(cause);
    }
}

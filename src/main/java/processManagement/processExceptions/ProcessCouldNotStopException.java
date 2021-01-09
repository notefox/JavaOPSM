package processManagement.processExceptions;

public class ProcessCouldNotStopException extends Throwable {
    public ProcessCouldNotStopException() {
        super("called process wasn't able to stop");
    }

    public ProcessCouldNotStopException(String message) {
        super(message);
    }

    public ProcessCouldNotStopException(String message, Throwable cause) {
        super(message, cause);
    }
}

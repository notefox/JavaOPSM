package ipcOverSockets.ProcessExceptions;

public class ProcessCouldNotStartException extends Throwable {
    public ProcessCouldNotStartException() {
        super("wasn't able to start process");
    }

    public ProcessCouldNotStartException(String message) {
        super(message);
    }

    public ProcessCouldNotStartException(Throwable cause) {
        super("wasn't able to start process", cause);
    }
}

package processManagement.processExceptions;

public class ProcessIsNotAliveException extends Throwable {
    public ProcessIsNotAliveException() {
        super("called process is not alive");
    }

    public ProcessIsNotAliveException(String message) {
        super(message);
    }

    public ProcessIsNotAliveException(Throwable cause) {
        super("called process is not alive", cause);
    }
}

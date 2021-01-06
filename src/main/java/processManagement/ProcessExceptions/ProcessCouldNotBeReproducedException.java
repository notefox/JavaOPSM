package processManagement.ProcessExceptions;

public class ProcessCouldNotBeReproducedException extends Throwable {
    public ProcessCouldNotBeReproducedException() {
        super("couldn't reproduce Process");
    }

    public ProcessCouldNotBeReproducedException(String message) {
        super(message);
    }

    public ProcessCouldNotBeReproducedException(Throwable cause) {
        super(cause);
    }
}

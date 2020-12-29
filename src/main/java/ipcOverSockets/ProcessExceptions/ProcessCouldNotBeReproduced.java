package ipcOverSockets.ProcessExceptions;

public class ProcessCouldNotBeReproduced extends Throwable {
    public ProcessCouldNotBeReproduced() {
        super("couldn't reproduce Process");
    }

    public ProcessCouldNotBeReproduced(String message) {
        super(message);
    }

    public ProcessCouldNotBeReproduced(Throwable cause) {
        super(cause);
    }
}

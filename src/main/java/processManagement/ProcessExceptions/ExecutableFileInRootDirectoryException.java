package processManagement.ProcessExceptions;

public class ExecutableFileInRootDirectoryException extends Throwable {
	public ExecutableFileInRootDirectoryException() {
		super("no unwanted file is supposed to be in the root directory");
	}

	public ExecutableFileInRootDirectoryException(String message) {
		super(message);
	}

	public ExecutableFileInRootDirectoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutableFileInRootDirectoryException(Throwable cause) {
		super(cause);
	}
}

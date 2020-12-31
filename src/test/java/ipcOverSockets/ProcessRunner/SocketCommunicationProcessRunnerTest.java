package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ExecutableFileInRootDirectoryException;
import ipcOverSockets.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SocketCommunicationProcessRunnerTest {

	SocketCommunicationProcessRunner testRunner;

	@AfterEach
	void tearDown() {
		testRunner = null;
	}

	@Test
	void testSocketOpened_goodTest() throws IOException, InterpreterOrScriptNotDefinedException, ExecutableFileInRootDirectoryException, ProcessAlreadyStartedException, ProcessCouldNotStartException {
		File file = new File("test/test");
		ServerSocket seso = mock(ServerSocket.class);
		ScriptCreator sc = new ScriptCreator("test", file) {
			@Override
			public void afterRun(Process process) {

			}
		};
		testRunner = new SocketCommunicationProcessRunner("test", sc.buildRunnableProcessBuilder(), seso);
		testRunner.startProcess();

	}
}
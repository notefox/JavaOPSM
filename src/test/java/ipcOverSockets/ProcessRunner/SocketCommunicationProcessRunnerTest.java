package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SocketCommunicationProcessRunnerTest {

	SocketCommunicationProcessRunner testRunner;

	File mockedLoggerDir = mock(File.class);

	@BeforeEach
	void setUp() {
		when(mockedLoggerDir.canWrite()).thenReturn(true);
		when(mockedLoggerDir.delete()).thenReturn(true);
		when(mockedLoggerDir.exists()).thenReturn(true);
	}

	@AfterEach
	void tearDown() {
		testRunner = null;
	}

	@Test
	void testSocketOpened_goodTest() throws IOException, InterpreterOrScriptNotDefinedException, ExecutableFileInRootDirectoryException, ProcessAlreadyStartedException, ProcessCouldNotStartException, ProcessIsNotAliveException {
		File file = new File("test/test");
		ServerSocket seso = mock(ServerSocket.class);
		Socket mockedSocket = mock(Socket.class);
		when(seso.accept()).thenReturn(mockedSocket);
		ScriptCreator sc = new ScriptCreator("bash", file, mockedLoggerDir) {
			@Override
			public void afterRun(Process process) {
				//
			}
		};
		sc.addLineToScript("sleep 100");
		testRunner = new SocketCommunicationProcessRunner("test", sc.buildRunnableProcessBuilder(), mockedLoggerDir, seso);
		testRunner.startProcess();
		verify(seso, times(1)).accept();
		verify(mockedSocket, times(1)).getInputStream();
		verify(mockedSocket, times(1)).getOutputStream();
	}
}
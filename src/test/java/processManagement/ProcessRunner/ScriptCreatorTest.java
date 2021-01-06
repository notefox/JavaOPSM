package processManagement.ProcessRunner;

import processManagement.ProcessExceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScriptCreatorTest {

	ScriptCreator testCreator;
	String testInterpreter;
	File testFile;
	File mockedLoggerDir = mock(File.class);

	@BeforeEach
	void setUp() throws ExecutableFileInRootDirectoryException, IOException {
		testInterpreter = "python";
		testFile = new File("test/testFile.test");
		testCreator = new ScriptCreator(testInterpreter, testFile, mockedLoggerDir) {
			@Override
			public void afterRun(Process process) {
				// nothing
			}
		};
		when(mockedLoggerDir.canWrite()).thenReturn(true);
		when(mockedLoggerDir.delete()).thenReturn(true);
		when(mockedLoggerDir.exists()).thenReturn(true);
	}

	@AfterEach
	void tearDown() {
		testCreator = null;
	}

	@Test
	void standardScriptCreatorTestViaExternalProcessRunner_goodTest() throws ProcessAlreadyStartedException, IOException, InterruptedException,
			InterpreterOrScriptNotDefinedException, ProcessNotExitedYetException {
		testCreator.addLineToScript("print \"hello world\"");
		testCreator.addLineToScript("exit(2)");
		SimpleProcessRunner pr = new SimpleProcessRunner("test", ProcessRunnerType.SCRIPT_RUNNER, testCreator.buildRunnableProcessBuilder(), mockedLoggerDir) {
			@Override
			protected void afterStartProcessEvent() {

			}

			@Override
			protected void afterStopProcessEvent() {

			}

			@Override
			protected void afterRestartProcessEvent() {

			}

			@Override
			protected void afterFinishProcessEvent() {

			}
		};
		pr.startProcessWithoutRunningStartTest();
		pr.waitForProcess();
		assertEquals(2, pr.getLastExitCode());
		assertEquals("hello world", new BufferedReader(new InputStreamReader(pr.getProcessInputStream())).readLine());
		assertNull(new BufferedReader(new InputStreamReader(pr.getProcessErrorStream())).readLine());
	}

	@Test
	void ScriptCreatorInnerRunTest_goodTest() throws InterruptedException, InterpreterOrScriptNotDefinedException, IOException {
		testCreator.addLineToScript("print \"hello world\"");
		testCreator.addLineToScript("exit(2)");
		Process p = testCreator.runDirectly();
		assertEquals(2, p.exitValue());
		assertEquals("hello world", new BufferedReader(new InputStreamReader(p.getInputStream())).readLine());
		assertNull(new BufferedReader(new InputStreamReader(p.getInputStream())).readLine());
	}

	@Test
	void ScriptCreatorInnerStartTest_goodTest() throws ExecutableFileInRootDirectoryException, IOException, InterpreterOrScriptNotDefinedException, InterruptedException {
		testCreator = new ScriptCreator("bash", new File("test/testFile.test"), mockedLoggerDir) {
			@Override
			public void afterRun(Process process) {

			}
		};
		testCreator.addLineToScript("sleep 2");
		testCreator.addLineToScript("exit(2)");
		Process runningProcess = testCreator.startDirectly();
		assertTrue(runningProcess.isAlive());
		runningProcess.waitFor();
		assertEquals(2, runningProcess.exitValue());
	}

	@Test
	void createScriptInRootDirectory_badTest() throws ExecutableFileInRootDirectoryException {
		assertThrows(ExecutableFileInRootDirectoryException.class ,() -> new ScriptCreator("any", new File("test"), mockedLoggerDir) {
			@Override
			public void afterRun(Process process) {
				//
			}
		} );
	}

	@Test
	void getScriptPathTest_goodTest() throws ExecutableFileInRootDirectoryException, IOException {
		testCreator = new ScriptCreator("any", new File("test/testFile.test"), mockedLoggerDir) {
			@Override
			public void afterRun(Process process) {

			}
		};
		assertEquals("test/testFile.test", testCreator.getScriptPath());
	}

	@Test
	void giveUndefinableInterpreter_badTest() throws ExecutableFileInRootDirectoryException, IOException {
		testCreator = new ScriptCreator(null, new File("test/testFile.test"), mockedLoggerDir) {
			@Override
			public void afterRun(Process process) {
				//
			}
		};
		assertThrows(InterpreterOrScriptNotDefinedException.class, () -> testCreator.buildRunnableProcessBuilder());
		assertThrows(InterpreterOrScriptNotDefinedException.class, () -> testCreator.runDirectly());
		assertThrows(InterpreterOrScriptNotDefinedException.class, () -> testCreator.startDirectly());
	}
}
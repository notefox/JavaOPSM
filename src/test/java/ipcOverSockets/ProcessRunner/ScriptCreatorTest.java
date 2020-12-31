package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScriptCreatorTest {

	ScriptCreator testCreator;
	String testInterpreter;
	File testFile;

	@BeforeEach
	void setUp() {
		testInterpreter = "python";
		testFile = new File("test/testFile.test");
		testCreator = new ScriptCreator(testInterpreter, testFile) {
			@Override
			public void afterRun(Process process) {
				// nothing
			}
		};
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
		SimpleProcessRunner pr = new SimpleProcessRunner("test", ProcessRunnerType.SCRIPT_RUNNER, testCreator.buildRunnableProcessBuilder()) {
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
}
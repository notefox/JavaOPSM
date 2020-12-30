package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.InterpreterOrScriptNotDefinedException;
import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class ScriptCreatorTest {

	ScriptCreator testCreator;
	String testInterpreter;
	File testFile;

	@BeforeEach
	void setUp() throws IOException {
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
	void standardScriptCreatorTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, InterruptedException, ProcessIsNotAliveException, InterpreterOrScriptNotDefinedException {
		testCreator.addLineToScript("print \"hello world\"");
		testCreator.addLineToScript("exit(0)");
		ProcessRunner pr = new SimpleProcessRunner("test", testCreator.buildRunnableProcessBuilder()) {
			@Override
			protected void afterStartProcessEvent() {

			}

			@Override
			protected void afterStopProcessEvent() {

			}

			@Override
			protected void afterRestartProcessEvent() {

			}
		};
		pr.startProcess();
		pr.waitForProcess();
	}
}
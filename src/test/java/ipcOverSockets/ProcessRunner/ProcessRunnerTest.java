package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessRunnerTest {

    ProcessRunner pr;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void startStopProcess_goodTest() throws
            ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException,
            ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException {
        pr = new SimpleProcessRunner("sleep", "2") {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };
        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        pr.stopProcess();
        assertFalse(pr.isProcessAlive());
    }

    @Test
    void startWaitProcess_goodTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException,
            IOException, InterruptedException, ProcessIsNotAliveException {
        pr = new SimpleProcessRunner("sleep", "2") {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };
        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        pr.waitForProcess();
        assertFalse(pr.isProcessAlive());
    }

    @Test
    void reuseProcess_goodTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, InterruptedException, ProcessIsNotAliveException {
        pr = new SimpleProcessRunner("sleep", "2") {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };
        // first use
        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        pr.waitForProcess();
        assertFalse(pr.isProcessAlive());

        // second use
        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        pr.waitForProcess();
        assertFalse(pr.isProcessAlive());
    }

    @Test
    void restartProcess_goodTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, ProcessCouldNotStopException, ProcessIsNotAliveException, InterruptedException {
        pr = new SimpleProcessRunner("sleep", "2") {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };
        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        pr.restartProcess();
        assertTrue(pr.isProcessAlive());
        pr.waitForProcess();
        assertFalse(pr.isProcessAlive());
    }

    @Test
    void giveBadProcessCommand_badTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException {
        pr = new SimpleProcessRunner("nothing to execute here") {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };
        assertThrows(IOException.class, () -> pr.startProcess());
    }

    @Test
    void giveProcessBuilder_goodTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException {
        ProcessBuilder builderMock = mock(ProcessBuilder.class);
        Process processMock = mock(Process.class);

        when(builderMock.start()).thenReturn(processMock);
        when(processMock.isAlive()).thenReturn(true);

        pr = new SimpleProcessRunner("test", builderMock) {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };

        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        verify(builderMock, times(1)).start();
        verify(processMock, times(1)).isAlive();

        assertThrows(ProcessCouldNotStopException.class,() -> pr.stopProcess());
        verify(processMock, times(1)).destroy();
        verify(processMock, times(1)).destroyForcibly();
    }

    @Test
    void giveReproducibleProcess_goodTest() throws ProcessCouldNotStartException,
            ProcessAlreadyStartedException, IOException, ProcessCouldNotBeReproduced {

        Process processMock = mock(Process.class);
        when(processMock.info()).thenReturn(new ProcessHandle.Info() {
            @Override
            public Optional<String> command() {
                return Optional.of("/bin/sleep");
            }

            @Override
            public Optional<String> commandLine() {
                return Optional.of("/bin/sleep 15");
            }

            @Override
            public Optional<String[]> arguments() {
                return Optional.of(new String[]{"15"});
            }

            @Override
            public Optional<Instant> startInstant() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> totalCpuDuration() {
                return Optional.empty();
            }

            @Override
            public Optional<String> user() {
                return Optional.empty();
            }
        });
        pr = new SimpleProcessRunner("test", processMock) {
            @Override
            protected void afterStartProcessEvent() {
                //
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }
        };
        pr.startProcess();
        assertEquals(pr.getProcessInfo().command(), processMock.info().command());
        assertArrayEquals(pr.getProcessInfo().arguments().get(), processMock.info().arguments().get());
        assertEquals(pr.getProcessInfo().commandLine().get(), processMock.info().commandLine().get());
    }

    @Test
    void giveDiedUnReproducibleProcess_badTest() {
        Process processMock = mock(Process.class);
        when(processMock.info()).thenReturn(new ProcessHandle.Info() {
            @Override
            public Optional<String> command() {
                return Optional.empty();
            }

            @Override
            public Optional<String> commandLine() {
                return Optional.empty();
            }

            @Override
            public Optional<String[]> arguments() {
                return Optional.empty();
            }

            @Override
            public Optional<Instant> startInstant() {
                return Optional.empty();
            }

            @Override
            public Optional<Duration> totalCpuDuration() {
                return Optional.empty();
            }

            @Override
            public Optional<String> user() {
                return Optional.empty();
            }
        });
        assertThrows(ProcessCouldNotBeReproduced.class, () -> pr = new SimpleProcessRunner("test", processMock) {
            @Override
            protected void afterStartProcessEvent() {

            }

            @Override
            protected void afterStopProcessEvent() {

            }

            @Override
            protected void afterRestartProcessEvent() {

            }
        } );
        verify(processMock, Mockito.times(2)).info();
    }
}
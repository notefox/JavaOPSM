package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.reflect.Executable;
import java.time.Duration;
import java.time.Instant;
import java.time.chrono.ThaiBuddhistEra;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProcessRunnerTest {

    ProcessRunner pr;
    File mockedLoggerDir = mock(File.class);

    @BeforeEach
    void setUp() {
        when(mockedLoggerDir.canWrite()).thenReturn(true);
        when(mockedLoggerDir.delete()).thenReturn(true);
        when(mockedLoggerDir.exists()).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void startStopProcess_goodTest() throws
            ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException,
            ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException {
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir,"sleep", "2") {
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

            @Override
            protected void afterFinishProcessEvent() {
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
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "sleep", "2") {
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

            @Override
            protected void afterFinishProcessEvent() {
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
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "sleep", "2") {
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

            @Override
            protected void afterFinishProcessEvent() {
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
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "sleep", "2") {
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

            @Override
            protected void afterFinishProcessEvent() {
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
    void giveBadProcessCommand_badTest() throws IOException {
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "nothing to execute here") {
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

            @Override
            protected void afterFinishProcessEvent() {
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

        pr = new SimpleProcessRunner("test",ProcessRunnerType.CUSTOM, builderMock, mockedLoggerDir) {
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

            @Override
            protected void afterFinishProcessEvent() {
                //
            }
        };

        pr.startProcess();
        assertTrue(pr.isProcessAlive());
        verify(builderMock, atLeast(1)).start();
        verify(processMock, atLeast(1)).isAlive();

        assertThrows(ProcessCouldNotStopException.class,() -> pr.stopProcess());
        verify(processMock, atLeast(1)).destroy();
        verify(processMock, atLeast(1)).destroyForcibly();
    }

    @Test
    void giveReproducibleProcess_goodTest() throws ProcessCouldNotStartException,
            ProcessAlreadyStartedException, IOException, ProcessCouldNotBeReproducedException {

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
        pr = new SimpleProcessRunner("test", processMock, mockedLoggerDir) {
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

            @Override
            protected void afterFinishProcessEvent() {
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
        assertThrows(ProcessCouldNotBeReproducedException.class, () -> pr = new SimpleProcessRunner("test", processMock, mockedLoggerDir) {
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

            @Override
            protected void afterFinishProcessEvent() {
                //
            }
        } );
        verify(processMock, Mockito.times(2)).info();
    }

    @Test
    void startFinishEventMethodTesting_goodTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, InterruptedException {
        Thread startEvent = mock(Thread.class);
        Thread finishEvent = mock(Thread.class);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "sleep", "0") {
            @Override
            protected void afterStartProcessEvent() {
                startEvent.start();
            }

            @Override
            protected void afterStopProcessEvent() {
                //
            }

            @Override
            protected void afterRestartProcessEvent() {
                //
            }

            @Override
            protected void afterFinishProcessEvent() {
                finishEvent.start();
            }
        };
        Thread.sleep(1);
        pr.startProcessWithoutRunningStartTest();
        verify(startEvent, times(1)).start();
        verify(finishEvent, times(1)).start();
    }

    @Test
    void restartStopEventMethodTesting_goodTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException, ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException {
        Thread start = mock(Thread.class);
        Thread stop = mock(Thread.class);
        Thread restart = mock(Thread.class);
        Thread finish = mock(Thread.class);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "sleep", "3") {
            @Override
            protected void afterStartProcessEvent() {
                start.start();
            }

            @Override
            protected void afterStopProcessEvent() {
                stop.start();
            }

            @Override
            protected void afterRestartProcessEvent() {
                restart.start();
            }

            @Override
            protected void afterFinishProcessEvent() {
                finish.start();
            }
        };
        pr.startProcess();
        pr.restartProcess();
        pr.waitForProcess();
        verify(start, times(2)).start();
        verify(stop, times(1)).start();
        verify(restart, times(1)).start();
        verify(finish, times(2)).start();
    }

    @Test
    void giveNoProcessCommandStrings_badTest() {
        assertThrows(NullPointerException.class, () -> new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir) {
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
        });
    }

    @Test
    void startAlreadyStartedProcess_badTest() throws ProcessCouldNotStartException, ProcessAlreadyStartedException, IOException {
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedLoggerDir, "sleep", "5") {
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
        pr.startProcess();
        assertThrows(ProcessAlreadyStartedException.class, () -> pr.startProcess());
    }

    @Test
    void processRefusedToStartTest_badTest() throws IOException {
        ProcessBuilder mockedPB = mock(ProcessBuilder.class);
        Process mockedP = mock(Process.class);
        when(mockedPB.start()).thenReturn(mockedP);
        when(mockedP.isAlive()).thenReturn(false);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedPB, mockedLoggerDir) {
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
        assertThrows(ProcessCouldNotStartException.class, () -> pr.startProcess());
    }

    @Test
    void restartNotRunningProcessTest_badTest() throws IOException, ProcessAlreadyStartedException, ProcessCouldNotStartException {
        ProcessBuilder mockedPB = mock(ProcessBuilder.class);
        Process mockedP = mock(Process.class);
        when(mockedPB.start()).thenReturn(mockedP);
        when(mockedP.isAlive()).thenReturn(false);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedPB, mockedLoggerDir) {
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
        assertThrows(ProcessIsNotAliveException.class, () -> pr.restartProcess());
    }

    @Test
    void processCouldNotBeStoppedTest_badTest() throws IOException, ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException, ProcessAlreadyStartedException, ProcessCouldNotStartException {
        ProcessBuilder mockedPB = mock(ProcessBuilder.class);
        Process mockedP = mock(Process.class);
        when(mockedPB.start()).thenReturn(mockedP);
        when(mockedP.isAlive()).thenReturn(true);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedPB, mockedLoggerDir) {
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
        pr.startProcess();
        assertThrows(ProcessCouldNotStopException.class, () -> pr.stopProcess());
    }

    @Test
    void waitForNotAliveProcessTest_goodTest() throws IOException, InterruptedException, ProcessIsNotAliveException, ProcessAlreadyStartedException, ProcessCouldNotStartException, ProcessCouldNotStopException {
        ProcessBuilder mockedPB = mock(ProcessBuilder.class);
        Process mockedP = mock(Process.class);
        when(mockedPB.start()).thenReturn(mockedP);
        when(mockedP.isAlive()).thenReturn(true);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedPB, mockedLoggerDir) {
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
        pr.startProcess();
        when(mockedP.isAlive()).thenReturn(false);
        pr.waitForProcess();
    }

    @Test
    void unableToStopProcessAfterWaitingForItTest_badTest() throws IOException, ProcessAlreadyStartedException, ProcessCouldNotStartException {
        ProcessBuilder mockedPB = mock(ProcessBuilder.class);
        Process mockedP = mock(Process.class);
        when(mockedPB.start()).thenReturn(mockedP);
        when(mockedP.isAlive()).thenReturn(true);
        pr = new SimpleProcessRunner("test", ProcessRunnerType.CUSTOM, mockedPB, mockedLoggerDir) {
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
        pr.startProcess();
        assertThrows(ProcessCouldNotStopException.class, () -> pr.waitForProcess(15L));
    }
}
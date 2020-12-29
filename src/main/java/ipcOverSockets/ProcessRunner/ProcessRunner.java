package ipcOverSockets.ProcessRunner;

import ipcOverSockets.ProcessExceptions.ProcessAlreadyStartedException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStartException;
import ipcOverSockets.ProcessExceptions.ProcessCouldNotStopException;
import ipcOverSockets.ProcessExceptions.ProcessIsNotAliveException;

import java.io.IOException;
import java.util.List;

public interface ProcessRunner {

    /**
     * Process starter
     * @throws IOException IOException may be thrown if being asked if alive
     * @throws ProcessCouldNotStartException is thrown, if the process wasn't alive after start
     * @throws ProcessAlreadyStartedException is thrown, if the process was already alive and needs to be stopped first
     */
    void startProcess() throws IOException, ProcessCouldNotStartException, ProcessAlreadyStartedException;

    /**
     * Process starter without running test afterwards (more useful if process is very short)
     * @throws IOException IOException may be thrown if being asked if alive
     * @throws ProcessAlreadyStartedException is thrown, if the process was already alive and needs to be stopped first
     */
    void startProcessWithoutRunningStartTest() throws IOException, ProcessCouldNotStartException, ProcessAlreadyStartedException;

    /**
     * Process stopper
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive in the first place
     * @throws ProcessCouldNotStopException is thrown, if the process did not stop after destroying
     * @throws InterruptedException is thrown, if the process of waiting for the process to destroy itself, was interrupted
     */
    void stopProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, InterruptedException;

    /**
     * Process Restarted
     *
     * simply calls stopProcess and startProcess after one another
     *
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive in the first place
     * @throws ProcessCouldNotStopException is thrown, if the process wasn't able to be stopped
     * @throws IOException can be thrown, when asking the process if it's alive
     * @throws ProcessCouldNotStartException is thrown, if the process wasn't able to start up again
     * @throws ProcessAlreadyStartedException is thrown if the process was already started by another source while restarting
     */
    void restartProcess() throws ProcessIsNotAliveException, ProcessCouldNotStopException, IOException, ProcessCouldNotStartException, ProcessAlreadyStartedException, InterruptedException;

    /**
     * Process Restarted
     *
     * simply calls stopProcess and startProcess after one another
     *
     * @throws ProcessIsNotAliveException is thrown, if the process wasn't alive in the first place
     * @throws ProcessCouldNotStopException is thrown, if the process wasn't able to be stopped
     * @throws IOException can be thrown, when asking the process if it's alive
     * @throws ProcessAlreadyStartedException is thrown if the process was already started by another source while restarting
     */
    void restartProcessWithoutRunningStartTest() throws ProcessIsNotAliveException, ProcessCouldNotStopException, IOException, ProcessAlreadyStartedException, InterruptedException;

    /**
     * returns a boolean for the process is alive call
     * (also returns false, even if Process is not defined yet)
     * @return boolean
     */
    boolean isProcessAlive();

    /**
     * calls the waitFor method for the currently running Process
     * @throws ProcessIsNotAliveException is thrown, if Process isn't running, therefore it can't wait for it to finish
     * @throws InterruptedException is thrown, if the waitFor method call was interrupted by an interrupt
     */
    void waitForProcess() throws ProcessIsNotAliveException, InterruptedException;

    /**
     * getter for process ID
     * @return long
     */
    long getPID() throws ProcessIsNotAliveException;

    /**
     * returns Process Information handle
     * @return ProcessHandle.Info
     */
    ProcessHandle.Info getProcessInfo();

    /**
     * gets executed command
     * @return argument List<String>
     */
    List<String> getCommand();
}

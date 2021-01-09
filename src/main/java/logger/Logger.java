package logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Logger is a custom made open source log service
 * @author notefox (https://github.com/NoteFox)
 */
public class Logger extends Thread {

    // logger instance
    public static Logger instance;

    // base file prefix
    private final String baseFile;

    // current writing Directory where Log Files are inserted
    private File currentWritingDir;

    // current writing File where Log Entries are inserted in
    private File currentWritingFile;

    // buffer list for Log Entries
    private final List<LogEntry> BUFFER_LIST = new ArrayList<>();

    // date and time formatter for log entries, directory and file names
    private final SimpleDateFormat dirFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat fileFormatter = new SimpleDateFormat("HH-mm-ss z");
    private final SimpleDateFormat logFormatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

    // initial defined variables
    // default logging directory
    private final File logDefaultDir;
    // max log file
    private final int maxLogFileSize;
    // std output bit
    private final boolean logTerminalOutput;

    // exit bit
    private boolean stop = false;

    // info bit for logger
    private boolean isWaiting = false;

    /**
     * Logger constructor
     * @param logDefaultDir dir where logging will take place
     * @param maxLogFileSize max log file size
     * @param logTerminalOutput std output bit
     */
    public Logger(String filePrefix, File logDefaultDir, int maxLogFileSize, boolean logTerminalOutput) throws IOException {
        if (!logDefaultDir.exists()) {
            boolean result = logDefaultDir.mkdirs();
            if (!result) {
                throw new IOException("couldn't create needed directory: " + logDefaultDir);
            }
        }
        this.logDefaultDir = logDefaultDir;
        this.maxLogFileSize = maxLogFileSize;
        this.logTerminalOutput = logTerminalOutput;

        this.baseFile = filePrefix;

        long date = System.currentTimeMillis();
        this.currentWritingDir = new File(logDefaultDir.getPath() + "/" + dirFormatter.format(date));
        this.currentWritingFile = new File(currentWritingDir + "/" + baseFile.replace(".txt", "") + "["+ fileFormatter.format(date) +"].txt");

        if (instance == null)
            instance = this;
    }

    /**
     * getter for current buffer list ( not real buffer list )
     * @return ArrayList<LogEntry>
     */
    public List<LogEntry> getBUFFER_LIST() {
        return new ArrayList<>(BUFFER_LIST);
    }

    /**
     * log entry adder
     * @param type type of entry
     * @param TAG entry tag
     * @param message entry message
     */
    public void addLogEntry(LogType type, String TAG, String message) {
        synchronized (this) {
            BUFFER_LIST.add(new LogEntry(type, TAG, message));
        }
        if (isWaiting)
            this.interrupt();
    }

    /**
     * exit method for Thread
     */
    public void stopLogger() {
        this.stop = true;
        interrupt();
    }

    /**
     * method to create and swap to next file if current file is full
     * @throws IOException is thrown, if new file can't be created
     */
    private void swapToNextFileIfNecessary() throws IOException {
        if (this.currentWritingFile.length() / 1000 > maxLogFileSize) {
            this.currentWritingFile = new File(currentWritingDir + "/" + baseFile.replace(".txt", "") + "["+ fileFormatter.format(System.currentTimeMillis()) +"].txt");

            if (!this.currentWritingFile.exists())
                this.currentWritingFile.createNewFile();
        }
    }

    /**
     * method to create and swap to next directory if new entry into file happens in next day
     * @throws IOException
     */
    private void swapToNextDayDirIfNecessary() throws IOException {
        Date date = new Date(System.currentTimeMillis());

        if (!this.currentWritingDir.equals(logDefaultDir + dirFormatter.format(date))) {
            this.currentWritingDir = new File(logDefaultDir.getPath() + "/" + dirFormatter.format(date));

            if (!this.currentWritingDir.exists())
                if (!this.currentWritingDir.mkdir()) {
                    throw new IOException("couldn't create new writing dir");
                }

            this.currentWritingFile = new File(currentWritingDir + "/" + baseFile.replace(".txt", "") + "["+ fileFormatter.format(System.currentTimeMillis()) +"].txt");

            if (!this.currentWritingFile.exists())
                this.currentWritingFile.createNewFile();
        }
    }

    /**
     * run
     */
    @Override
    public synchronized void run() {
        do {
            try {
                swapToNextDayDirIfNecessary();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                swapToNextFileIfNecessary();
            } catch (IOException e) {
                e.printStackTrace();
            }

            synchronized (this) {
                if (BUFFER_LIST.isEmpty()) {
                    isWaiting = true;
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // kindly ignore
                    }
                    isWaiting = false;
                } else {
                    try {
                        writeEntry(BUFFER_LIST.get(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    BUFFER_LIST.remove(0);
                }
            }

            if (stop)
                return;
        } while (true);
    }

    /**
     * entry writer into current file
     * @param entry entry to write in
     * @throws IOException is thrown, if current file can't be accessed
     */
    private void writeEntry(LogEntry entry) throws IOException {
        String temp = "";

        Date date = new Date(System.currentTimeMillis());
        String input = entry.getType() + " | " +
                logFormatter.format(date) + " | " + entry.getTAG() + " : " + entry.getMessage();

        if (logTerminalOutput)
            System.out.println(input);

        synchronized (this) {
            /* BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(currentWritingFile)));

            while (br.ready()) {
                temp += br.readLine() + "\n";
            }

            temp += input + "\n";
            br.close(); */

            BufferedWriter bw = new BufferedWriter(new FileWriter(currentWritingFile, true));
            bw.write(input + "\n");
            bw.close();
        }
    }
}

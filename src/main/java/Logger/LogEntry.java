package Logger;

/**
 * log entry class
 */
public class LogEntry {

    /**
     * log type
     */
    public LogType type;

    /**
     * log tag
     */
    public String TAG;

    /**
     * log message
     */
    public String message;

    /**
     * constructor
     * @param type type
     * @param TAG tag
     * @param message message
     */
    public LogEntry(LogType type, String TAG, String message) {
        this.type = type;
        this.TAG = TAG;
        this.message = message;
    }

    /**
     * type getter
     * @return LogType
     */
    public LogType getType() {
        return type;
    }

    /**
     * tag getter
     * @return String
     */
    public String getTAG() {
        return TAG;
    }

    /**
     * message getter
     * @return String
     */
    public String getMessage() {
        return message;
    }
}

package Logger;

/**
 * log type enum
 */
public enum LogType {
    DEBUG("[DEBUG]"),
    ERROR("[ERROR]"),
    INFO("[INFO]"),
    MESSAGE("[MESSAGE]"),
    WARNING("[WARNING]");

    /**
     * constructor
     * @param s log type
     */
    LogType(String s) {

    }
}

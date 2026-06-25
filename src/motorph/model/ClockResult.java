package motorph.model;

/**
 * Represents the result of a clock-in or clock-out operation.
 * Replaces the null/String return pattern where null meant success
 * and a String meant failure, which was ambiguous for callers.
 *
 * Usage:
 *   ClockResult result = attendanceReader.clockIn(...);
 *   if (result.isSuccess()) { ... }
 *   else { showMessage(result.getMessage()); }
 */
public class ClockResult {

    private final boolean success;
    private final String  message;

    private ClockResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /** Creates a successful result with no message. */
    public static ClockResult ok() {
        return new ClockResult(true, null);
    }

    /** Creates a failed result with an error message. */
    public static ClockResult fail(String message) {
        return new ClockResult(false, message);
    }

    /** Returns true if the operation succeeded. */
    public boolean isSuccess() { return success; }

    /** Returns true if the operation failed. */
    public boolean isFailure() { return !success; }

    /**
     * Returns the error message if the operation failed, or null if it succeeded.
     *
     * @return error message or null
     */
    public String getMessage() { return message; }
}
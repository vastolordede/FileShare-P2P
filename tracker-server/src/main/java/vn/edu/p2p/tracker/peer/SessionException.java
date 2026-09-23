package vn.edu.p2p.tracker.peer;

public final class SessionException extends Exception {
    private final String errorCode;

    public SessionException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SessionException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}

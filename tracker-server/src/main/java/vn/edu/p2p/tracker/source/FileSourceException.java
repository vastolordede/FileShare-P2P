package vn.edu.p2p.tracker.source;

public final class FileSourceException extends Exception {
    private final String errorCode;

    public FileSourceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public FileSourceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}

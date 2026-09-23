package vn.edu.p2p.peer.auth;

public final class AuthClientException extends Exception {
    private final String errorCode;

    public AuthClientException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AuthClientException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}

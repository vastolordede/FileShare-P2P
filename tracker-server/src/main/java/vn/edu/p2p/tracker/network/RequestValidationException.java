package vn.edu.p2p.tracker.network;

final class RequestValidationException extends Exception {
    private final String errorCode;

    RequestValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    String errorCode() {
        return errorCode;
    }
}

package vn.edu.p2p.common.protocol;

import java.io.IOException;

public class ProtocolException extends IOException {
    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}

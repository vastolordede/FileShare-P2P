package vn.edu.p2p.tracker.network;

import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;

final class TrackerErrorResponses {
    private TrackerErrorResponses() {
    }

    static MessageEnvelope forRequest(
            MessageEnvelope request,
            String errorCode,
            String message
    ) {
        if (request == null) {
            return protocolError("unknown", errorCode, message);
        }
        return MessageEnvelope.error(
                responseTypeFor(request.type()),
                safeRequestId(request.requestId()),
                errorCode,
                message
        );
    }

    static MessageEnvelope protocolError(
            String requestId,
            String errorCode,
            String message
    ) {
        return MessageEnvelope.error(
                MessageType.ERROR,
                safeRequestId(requestId),
                errorCode,
                message
        );
    }

    static MessageType responseTypeFor(MessageType requestType) {
        if (requestType == null) {
            return MessageType.ERROR;
        }
        return switch (requestType) {
            case LOGIN_REQUEST, LOGIN_RESPONSE -> MessageType.LOGIN_RESPONSE;
            case LOGOUT_REQUEST, LOGOUT_RESPONSE -> MessageType.LOGOUT_RESPONSE;
            case HEARTBEAT, HEARTBEAT_ACK -> MessageType.HEARTBEAT_ACK;
            case ERROR -> MessageType.ERROR;
        };
    }

    private static String safeRequestId(String requestId) {
        return requestId == null || requestId.isBlank() ? "unknown" : requestId;
    }
}

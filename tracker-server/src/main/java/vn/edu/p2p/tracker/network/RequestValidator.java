package vn.edu.p2p.tracker.network;

import vn.edu.p2p.common.dto.HeartbeatRequest;
import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LogoutRequest;
import vn.edu.p2p.common.protocol.MessageEnvelope;

import java.util.UUID;

final class RequestValidator {
    private static final int MAX_REQUEST_ID_LENGTH = 128;
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_DEVICE_NAME_LENGTH = 100;

    void validateEnvelope(MessageEnvelope request) throws RequestValidationException {
        if (request == null) {
            throw invalid("Missing request envelope.");
        }
        if (request.version() != MessageEnvelope.CURRENT_VERSION) {
            throw new RequestValidationException(
                    "UNSUPPORTED_PROTOCOL_VERSION",
                    "Unsupported protocol version: " + request.version()
            );
        }
        if (request.requestId() == null || request.requestId().isBlank()) {
            throw invalid("Missing requestId.");
        }
        if (request.requestId().length() > MAX_REQUEST_ID_LENGTH) {
            throw invalid("requestId is too long.");
        }
        if (request.payload() == null || request.payload().isNull()) {
            throw invalid("Missing request payload.");
        }
    }

    void validateLogin(LoginRequest request) throws RequestValidationException {
        if (request == null) {
            throw invalid("Missing login payload.");
        }
        if (request.username() == null || request.username().isBlank()) {
            throw invalid("Missing username.");
        }
        if (request.username().trim().length() > MAX_USERNAME_LENGTH) {
            throw invalid("Username is too long.");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw invalid("Missing password.");
        }
        if (request.listeningPort() < 1 || request.listeningPort() > 65535) {
            throw new RequestValidationException(
                    "INVALID_PORT",
                    "Invalid Peer listening port."
            );
        }
        requireUuid(request.peerId(), "INVALID_PEER_ID", "peerId");
        if (request.deviceName() != null
                && request.deviceName().trim().length() > MAX_DEVICE_NAME_LENGTH) {
            throw invalid("Device name is too long.");
        }
    }

    void validateLogout(LogoutRequest request) throws RequestValidationException {
        if (request == null) {
            throw invalid("Missing logout payload.");
        }
        requireUuid(request.sessionId(), "INVALID_SESSION", "sessionId");
    }

    void validateHeartbeat(HeartbeatRequest request) throws RequestValidationException {
        if (request == null) {
            throw invalid("Missing heartbeat payload.");
        }
        requireUuid(request.sessionId(), "INVALID_SESSION", "sessionId");
    }

    private static void requireUuid(
            String raw,
            String errorCode,
            String fieldName
    ) throws RequestValidationException {
        if (raw == null || raw.isBlank()) {
            throw new RequestValidationException(
                    errorCode,
                    "Missing " + fieldName + "."
            );
        }
        try {
            UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new RequestValidationException(
                    errorCode,
                    fieldName + " must be a valid UUID."
            );
        }
    }

    private static RequestValidationException invalid(String message) {
        return new RequestValidationException("INVALID_REQUEST", message);
    }
}

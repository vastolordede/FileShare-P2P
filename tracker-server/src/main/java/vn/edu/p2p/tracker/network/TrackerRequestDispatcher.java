package vn.edu.p2p.tracker.network;

import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LoginResponse;
import vn.edu.p2p.common.dto.LogoutRequest;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;
import vn.edu.p2p.common.protocol.ProtocolCodec;
import vn.edu.p2p.tracker.auth.AuthException;
import vn.edu.p2p.tracker.auth.AuthService;

public final class TrackerRequestDispatcher {
    private final AuthService authService;

    public TrackerRequestDispatcher(AuthService authService) {
        this.authService = authService;
    }

    public MessageEnvelope dispatch(MessageEnvelope request, String remoteIp) {
        if (request.version() != MessageEnvelope.CURRENT_VERSION) {
            return MessageEnvelope.error(
                    responseTypeFor(request.type()),
                    request.requestId(),
                    "UNSUPPORTED_PROTOCOL_VERSION",
                    "Unsupported protocol version: " + request.version()
            );
        }

        try {
            return switch (request.type()) {
                case LOGIN_REQUEST -> handleLogin(request, remoteIp);
                case LOGOUT_REQUEST -> handleLogout(request);
                case HEARTBEAT -> MessageEnvelope.error(
                        MessageType.HEARTBEAT_ACK,
                        request.requestId(),
                        "NOT_IMPLEMENTED_YET",
                        "Heartbeat persistence is implemented in Week 3."
                );
                default -> MessageEnvelope.error(
                        responseTypeFor(request.type()),
                        request.requestId(),
                        "UNSUPPORTED_MESSAGE",
                        "Unsupported message type: " + request.type()
                );
            };
        } catch (AuthException e) {
            return MessageEnvelope.error(
                    responseTypeFor(request.type()),
                    request.requestId(),
                    e.errorCode(),
                    e.getMessage()
            );
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return MessageEnvelope.error(
                    responseTypeFor(request.type()),
                    request.requestId(),
                    "SERVER_ERROR",
                    "Tracker failed to process the request."
            );
        }
    }

    private MessageEnvelope handleLogin(
            MessageEnvelope request,
            String remoteIp
    ) throws Exception {
        LoginRequest payload = ProtocolCodec.fromPayload(
                request.payload(),
                LoginRequest.class
        );

        LoginResponse response = authService.login(payload, remoteIp);
        return MessageEnvelope.success(
                MessageType.LOGIN_RESPONSE,
                request.requestId(),
                ProtocolCodec.toPayload(response)
        );
    }

    private MessageEnvelope handleLogout(MessageEnvelope request) throws Exception {
        LogoutRequest payload = ProtocolCodec.fromPayload(
                request.payload(),
                LogoutRequest.class
        );
        authService.logout(payload);

        return MessageEnvelope.success(
                MessageType.LOGOUT_RESPONSE,
                request.requestId(),
                null
        );
    }

    private static MessageType responseTypeFor(MessageType requestType) {
        return switch (requestType) {
            case LOGIN_REQUEST, LOGIN_RESPONSE -> MessageType.LOGIN_RESPONSE;
            case LOGOUT_REQUEST, LOGOUT_RESPONSE -> MessageType.LOGOUT_RESPONSE;
            case HEARTBEAT, HEARTBEAT_ACK -> MessageType.HEARTBEAT_ACK;
        };
    }
}

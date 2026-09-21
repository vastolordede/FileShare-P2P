package vn.edu.p2p.peer.auth;

import vn.edu.p2p.common.dto.HeartbeatRequest;
import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LogoutRequest;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;
import vn.edu.p2p.common.protocol.ProtocolCodec;

import java.util.UUID;

/**
 * Week 1 responsibility: build protocol messages only.
 *
 * Week 2 will send them through TrackerConnection.
 */
public final class AuthClientService {

    public MessageEnvelope createLoginRequest(
            String username,
            String password,
            int listeningPort
    ) {
        LoginRequest payload = new LoginRequest(
                username,
                password,
                listeningPort
        );

        return MessageEnvelope.request(
                MessageType.LOGIN_REQUEST,
                UUID.randomUUID().toString(),
                ProtocolCodec.toPayload(payload)
        );
    }

    public MessageEnvelope createHeartbeatRequest(String sessionId) {
        return MessageEnvelope.request(
                MessageType.HEARTBEAT,
                UUID.randomUUID().toString(),
                ProtocolCodec.toPayload(new HeartbeatRequest(sessionId))
        );
    }

    public MessageEnvelope createLogoutRequest(String sessionId) {
        return MessageEnvelope.request(
                MessageType.LOGOUT_REQUEST,
                UUID.randomUUID().toString(),
                ProtocolCodec.toPayload(new LogoutRequest(sessionId))
        );
    }
}

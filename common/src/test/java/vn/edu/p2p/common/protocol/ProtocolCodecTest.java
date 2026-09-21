package vn.edu.p2p.common.protocol;

import org.junit.jupiter.api.Test;
import vn.edu.p2p.common.dto.LoginRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProtocolCodecTest {

    @Test
    void shouldRoundTripLengthPrefixedLoginRequest() throws Exception {
        String requestId = UUID.randomUUID().toString();
        LoginRequest login = new LoginRequest("dang", "secret", 7001);

        MessageEnvelope original = MessageEnvelope.request(
                MessageType.LOGIN_REQUEST,
                requestId,
                ProtocolCodec.toPayload(login)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProtocolCodec.write(out, original);

        MessageEnvelope decoded = ProtocolCodec.read(
                new ByteArrayInputStream(out.toByteArray())
        );

        assertEquals(MessageType.LOGIN_REQUEST, decoded.type());
        assertEquals(requestId, decoded.requestId());
        assertNull(decoded.status());

        LoginRequest decodedLogin = ProtocolCodec.fromPayload(
                decoded.payload(),
                LoginRequest.class
        );

        assertEquals(login, decodedLogin);
    }
}

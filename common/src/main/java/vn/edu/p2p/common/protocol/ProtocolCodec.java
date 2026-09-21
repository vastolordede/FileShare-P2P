package vn.edu.p2p.common.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * TCP framing for control messages:
 *
 * [4-byte signed big-endian JSON length][N bytes UTF-8 JSON]
 *
 * Do not assume one Socket.read() call equals one complete message.
 */
public final class ProtocolCodec {
    public static final int MAX_JSON_BYTES = 1_048_576; // 1 MiB for control messages

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProtocolCodec() {
    }

    public static JsonNode toPayload(Object value) {
        return MAPPER.valueToTree(value);
    }

    public static <T> T fromPayload(JsonNode payload, Class<T> type)
            throws JsonProcessingException {
        if (payload == null || payload.isNull()) {
            return null;
        }
        return MAPPER.treeToValue(payload, type);
    }

    public static String toJson(MessageEnvelope message)
            throws JsonProcessingException {
        return MAPPER.writeValueAsString(message);
    }

    public static MessageEnvelope fromJson(String json)
            throws JsonProcessingException {
        return MAPPER.readValue(json, MessageEnvelope.class);
    }

    public static void write(OutputStream output, MessageEnvelope message)
            throws IOException {
        byte[] json = MAPPER.writeValueAsBytes(message);

        if (json.length == 0 || json.length > MAX_JSON_BYTES) {
            throw new ProtocolException(
                    "Invalid control-message size: " + json.length
            );
        }

        DataOutputStream dataOutput = new DataOutputStream(output);
        dataOutput.writeInt(json.length);
        dataOutput.write(json);
        dataOutput.flush();
    }

    public static MessageEnvelope read(InputStream input)
            throws IOException {
        DataInputStream dataInput = new DataInputStream(input);

        final int length;
        try {
            length = dataInput.readInt();
        } catch (EOFException e) {
            throw e;
        }

        if (length <= 0 || length > MAX_JSON_BYTES) {
            throw new ProtocolException(
                    "Invalid control-message length: " + length
            );
        }

        byte[] json = dataInput.readNBytes(length);
        if (json.length != length) {
            throw new EOFException(
                    "Connection closed before a complete message was received"
            );
        }

        try {
            return MAPPER.readValue(json, MessageEnvelope.class);
        } catch (JsonProcessingException e) {
            String raw = new String(json, StandardCharsets.UTF_8);
            throw new ProtocolException("Invalid JSON message: " + raw, e);
        }
    }
}

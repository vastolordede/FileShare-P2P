package vn.edu.p2p.peer.share;

import vn.edu.p2p.common.dto.FileMetadata;
import vn.edu.p2p.common.dto.ShareFileRequest;
import vn.edu.p2p.common.dto.ShareFileResponse;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;
import vn.edu.p2p.common.protocol.ProtocolCodec;
import vn.edu.p2p.common.protocol.ResponseStatus;
import vn.edu.p2p.peer.network.TrackerConnection;

import java.io.IOException;
import java.util.UUID;

/**
 * Sends a ShareFileRequest to the Tracker (T11) once FileMetadataBuilder has
 * produced the FileMetadata locally, and parses the ShareFileResponse.
 *
 * Mirrors AuthClientService's request/response pattern (build DTO -> wrap
 * in MessageEnvelope.request -> TrackerConnection.request -> unwrap payload).
 */
public final class ShareFileClientService {

    private final TrackerConnection connection;

    public ShareFileClientService(TrackerConnection connection) {
        this.connection = connection;
    }

    public ShareFileResponse publish(String sessionId, FileMetadata metadata)
            throws ShareFileClientException {

        ShareFileRequest payload = new ShareFileRequest(sessionId, metadata);

        MessageEnvelope request = MessageEnvelope.request(
                MessageType.SHARE_FILE_REQUEST,
                UUID.randomUUID().toString(),
                ProtocolCodec.toPayload(payload)
        );

        try {
            MessageEnvelope response = connection.request(request);

            if (response.status() == ResponseStatus.ERROR) {
                String detail = response.errorCode() != null
                        ? response.errorCode() + ": " + response.message()
                        : response.message();
                throw new ShareFileClientException(detail);
            }

            if (response.type() != MessageType.SHARE_FILE_RESPONSE) {
                throw new ShareFileClientException(
                        "Unexpected response type from Tracker: " + response.type()
                );
            }

            return ProtocolCodec.fromPayload(response.payload(), ShareFileResponse.class);
        } catch (IOException e) {
            throw new ShareFileClientException(
                    "Network error while sharing file: " + e.getMessage(), e
            );
        }
    }
}

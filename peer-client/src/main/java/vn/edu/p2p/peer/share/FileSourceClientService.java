package vn.edu.p2p.peer.share;

import vn.edu.p2p.common.dto.FileSourcesRequest;
import vn.edu.p2p.common.dto.FileSourcesResponse;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;
import vn.edu.p2p.common.protocol.ProtocolCodec;
import vn.edu.p2p.common.protocol.ResponseStatus;
import vn.edu.p2p.peer.network.TrackerConnection;

import java.io.IOException;
import java.util.UUID;

/**
 * Client-side call for FILE_SOURCES_REQUEST/RESPONSE (T18) - the DTOs and
 * Tracker-side handler already existed (see FileSourcesRequest/Response and
 * TrackerRequestDispatcher.handleFileSources), but nothing in peer-client
 * called it yet. Used here right after a Search result is selected, to show
 * who currently has the file; Sang's downloader can reuse this same class
 * later to pick sources for a transfer.
 */
public final class FileSourceClientService {

    private final TrackerConnection connection;

    public FileSourceClientService(TrackerConnection connection) {
        this.connection = connection;
    }

    public FileSourcesResponse findSources(String sessionId, long fileId)
            throws ShareFileClientException {

        FileSourcesRequest payload = new FileSourcesRequest(sessionId, fileId);

        MessageEnvelope request = MessageEnvelope.request(
                MessageType.FILE_SOURCES_REQUEST,
                UUID.randomUUID().toString(),
                ProtocolCodec.toPayload(payload)
        );

        try {
            MessageEnvelope response = connection.request(request);

            if (response.status() == ResponseStatus.ERROR) {
                throw new ShareFileClientException(errorText(response));
            }
            if (response.type() != MessageType.FILE_SOURCES_RESPONSE) {
                throw new ShareFileClientException("Unexpected response type: " + response.type());
            }

            return ProtocolCodec.fromPayload(response.payload(), FileSourcesResponse.class);
        } catch (IOException e) {
            throw new ShareFileClientException("Network error while fetching sources: " + e.getMessage(), e);
        }
    }

    private static String errorText(MessageEnvelope response) {
        return response.errorCode() != null
                ? response.errorCode() + ": " + response.message()
                : response.message();
    }
}

package vn.edu.p2p.common.dto;

/**
 * Sent from Peer to Tracker to publish a locally-built FileMetadata (T11).
 * sessionId identifies the sharing peer (resolved server-side to peer_id).
 */
public record ShareFileRequest(
        String sessionId,
        FileMetadata metadata
) {
}

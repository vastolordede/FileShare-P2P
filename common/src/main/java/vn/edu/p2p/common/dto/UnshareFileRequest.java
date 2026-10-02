package vn.edu.p2p.common.dto;

/** Stop sharing a file this peer previously announced (T22). */
public record UnshareFileRequest(
        String sessionId,
        long fileId
) {
}

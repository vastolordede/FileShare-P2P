package vn.edu.p2p.common.dto;

public record UnshareFileResponse(
        long fileId,
        boolean stopped
) {
}

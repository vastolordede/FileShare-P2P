package vn.edu.p2p.common.dto;

public record LoginResponse(
        String peerId,
        String sessionId,
        int heartbeatIntervalSeconds
) {
}

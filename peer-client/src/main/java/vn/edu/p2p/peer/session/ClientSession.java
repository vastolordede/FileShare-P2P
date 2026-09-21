package vn.edu.p2p.peer.session;

public record ClientSession(
        String peerId,
        String sessionId,
        int heartbeatIntervalSeconds
) {
}

package vn.edu.p2p.tracker.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PeerSessionRecord(
        UUID sessionId,
        long userId,
        UUID peerId,
        String ipAddress,
        int listeningPort,
        PeerStatus status,
        OffsetDateTime loginAt,
        OffsetDateTime lastSeen,
        OffsetDateTime logoutAt
) {
}

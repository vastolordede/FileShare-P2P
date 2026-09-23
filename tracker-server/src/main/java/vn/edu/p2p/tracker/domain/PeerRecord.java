package vn.edu.p2p.tracker.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PeerRecord(
        UUID peerId,
        long userId,
        String deviceName,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {
}

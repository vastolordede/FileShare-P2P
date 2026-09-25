package vn.edu.p2p.tracker.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileSourceRecord(
        long fileId,
        UUID peerId,
        String availabilityStatus,
        String ipAddress,
        int listeningPort,
        OffsetDateTime lastSeen
) {
}

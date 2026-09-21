package vn.edu.p2p.tracker.domain;

import java.time.OffsetDateTime;

public record UserRecord(
        long userId,
        String username,
        String passwordHash,
        boolean active,
        OffsetDateTime createdAt
) {
}

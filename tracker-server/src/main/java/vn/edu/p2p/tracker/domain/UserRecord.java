package vn.edu.p2p.tracker.domain;

import java.time.OffsetDateTime;

public record UserRecord(
        long userId,
        String username,
        String passwordHash,
        AccountStatus accountStatus,
        OffsetDateTime createdAt
) {
    public boolean isActive() {
        return accountStatus == AccountStatus.ACTIVE;
    }
}

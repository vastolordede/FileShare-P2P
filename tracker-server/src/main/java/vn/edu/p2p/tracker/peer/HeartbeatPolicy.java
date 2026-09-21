package vn.edu.p2p.tracker.peer;

import java.time.Duration;
import java.time.Instant;

/**
 * Pure Week 1 heartbeat rule, independent from DB/network.
 */
public record HeartbeatPolicy(
        Duration interval,
        Duration timeout
) {
    public HeartbeatPolicy {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("interval must be positive");
        }
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (timeout.compareTo(interval) <= 0) {
            throw new IllegalArgumentException("timeout must be greater than interval");
        }
    }

    public boolean isTimedOut(Instant lastSeen, Instant now) {
        return Duration.between(lastSeen, now).compareTo(timeout) > 0;
    }
}

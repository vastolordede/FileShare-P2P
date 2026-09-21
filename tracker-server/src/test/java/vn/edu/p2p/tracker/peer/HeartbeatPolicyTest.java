package vn.edu.p2p.tracker.peer;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartbeatPolicyTest {

    @Test
    void shouldTimeoutOnlyAfterConfiguredTimeout() {
        HeartbeatPolicy policy = new HeartbeatPolicy(
                Duration.ofSeconds(10),
                Duration.ofSeconds(30)
        );

        Instant lastSeen = Instant.parse("2026-09-21T10:00:00Z");

        assertFalse(policy.isTimedOut(
                lastSeen,
                lastSeen.plusSeconds(30)
        ));

        assertTrue(policy.isTimedOut(
                lastSeen,
                lastSeen.plusSeconds(31)
        ));
    }
}

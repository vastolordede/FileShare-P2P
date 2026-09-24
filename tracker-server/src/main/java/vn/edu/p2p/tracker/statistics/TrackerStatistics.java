package vn.edu.p2p.tracker.statistics;

import java.time.Instant;

public record TrackerStatistics(
        long registeredUsers,
        long onlinePeers,
        long sharedFiles,
        Instant capturedAt
) {
}

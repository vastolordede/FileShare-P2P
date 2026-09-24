package vn.edu.p2p.tracker.statistics;

import org.junit.jupiter.api.Test;
import vn.edu.p2p.tracker.repository.StatisticsRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackerStatisticsServiceTest {

    @Test
    void shouldCreateStatisticsSnapshot() throws Exception {
        Instant now = Instant.parse("2026-09-24T01:00:00Z");

        TrackerStatisticsService service = new TrackerStatisticsService(
                new FakeStatisticsRepository(7, 3, 5),
                Clock.fixed(now, ZoneOffset.UTC)
        );

        TrackerStatistics stats = service.snapshot();

        assertEquals(7, stats.registeredUsers());
        assertEquals(3, stats.onlinePeers());
        assertEquals(5, stats.sharedFiles());
        assertEquals(now, stats.capturedAt());
    }

    private record FakeStatisticsRepository(
            long users,
            long peers,
            long files
    ) implements StatisticsRepository {
        @Override
        public long countRegisteredUsers() {
            return users;
        }

        @Override
        public long countOnlinePeers() {
            return peers;
        }

        @Override
        public long countSharedFiles() {
            return files;
        }
    }
}

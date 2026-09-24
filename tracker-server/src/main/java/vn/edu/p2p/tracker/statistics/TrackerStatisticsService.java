package vn.edu.p2p.tracker.statistics;

import vn.edu.p2p.tracker.repository.StatisticsRepository;

import java.sql.SQLException;
import java.time.Clock;

public final class TrackerStatisticsService {
    private final StatisticsRepository repository;
    private final Clock clock;

    public TrackerStatisticsService(StatisticsRepository repository) {
        this(repository, Clock.systemUTC());
    }

    TrackerStatisticsService(StatisticsRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public TrackerStatistics snapshot() throws SQLException {
        return new TrackerStatistics(
                repository.countRegisteredUsers(),
                repository.countOnlinePeers(),
                repository.countSharedFiles(),
                clock.instant()
        );
    }
}

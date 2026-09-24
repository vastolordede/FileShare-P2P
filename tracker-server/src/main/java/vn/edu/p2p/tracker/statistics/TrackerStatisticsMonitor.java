package vn.edu.p2p.tracker.statistics;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class TrackerStatisticsMonitor implements AutoCloseable {
    private final TrackerStatisticsService statisticsService;
    private final Duration interval;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public TrackerStatisticsMonitor(
            TrackerStatisticsService statisticsService,
            Duration interval
    ) {
        if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("statistics interval must be positive");
        }
        this.statisticsService = statisticsService;
        this.interval = interval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new StatisticsThreadFactory()
        );
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        printSnapshot();

        long delayMillis = Math.max(1L, interval.toMillis());
        scheduler.scheduleWithFixedDelay(
                this::printSnapshot,
                delayMillis,
                delayMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private void printSnapshot() {
        try {
            TrackerStatistics stats = statisticsService.snapshot();
            System.out.printf(
                    "[Tracker stats] users=%d | onlinePeers=%d | sharedFiles=%d | at=%s%n",
                    stats.registeredUsers(),
                    stats.onlinePeers(),
                    stats.sharedFiles(),
                    stats.capturedAt()
            );
        } catch (SQLException e) {
            System.err.println(
                    "Tracker statistics database error: " + e.getMessage()
            );
        } catch (RuntimeException e) {
            System.err.println(
                    "Tracker statistics error: " + e.getMessage()
            );
        }
    }

    @Override
    public void close() {
        started.set(false);
        scheduler.shutdownNow();
    }

    private static final class StatisticsThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "tracker-statistics-monitor");
            thread.setDaemon(true);
            return thread;
        }
    }
}

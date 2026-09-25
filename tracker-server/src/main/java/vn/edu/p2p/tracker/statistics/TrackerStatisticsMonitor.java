package vn.edu.p2p.tracker.statistics;

import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class TrackerStatisticsMonitor implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(TrackerStatisticsMonitor.class.getName());

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
            LOG.info(() -> String.format(
                    "Tracker stats: users=%d, onlinePeers=%d, sharedFiles=%d, at=%s",
                    stats.registeredUsers(),
                    stats.onlinePeers(),
                    stats.sharedFiles(),
                    stats.capturedAt()
            ));
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Tracker statistics database error", e);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Tracker statistics error", e);
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

package vn.edu.p2p.tracker.peer;

import vn.edu.p2p.tracker.repository.PeerSessionRepository;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HeartbeatMonitor implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(HeartbeatMonitor.class.getName());

    private final PeerSessionRepository sessionRepository;
    private final Duration timeout;
    private final Duration scanInterval;
    private final Clock clock;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public HeartbeatMonitor(
            PeerSessionRepository sessionRepository,
            Duration timeout,
            Duration scanInterval
    ) {
        this(sessionRepository, timeout, scanInterval, Clock.systemUTC());
    }

    HeartbeatMonitor(
            PeerSessionRepository sessionRepository,
            Duration timeout,
            Duration scanInterval,
            Clock clock
    ) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (scanInterval == null || scanInterval.isZero() || scanInterval.isNegative()) {
            throw new IllegalArgumentException("scanInterval must be positive");
        }

        this.sessionRepository = sessionRepository;
        this.timeout = timeout;
        this.scanInterval = scanInterval;
        this.clock = clock;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new MonitorThreadFactory()
        );
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        long delayMillis = Math.max(1L, scanInterval.toMillis());
        scheduler.scheduleWithFixedDelay(
                this::safeScan,
                delayMillis,
                delayMillis,
                TimeUnit.MILLISECONDS
        );
    }

    int scanOnce() throws SQLException {
        OffsetDateTime now = OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        OffsetDateTime staleBefore = now.minus(timeout);
        return sessionRepository.expireStaleSessions(staleBefore);
    }

    private void safeScan() {
        try {
            int expired = scanOnce();
            if (expired > 0) {
                LOG.info(() -> String.format(
                        "Heartbeat monitor expired %d stale Peer session(s)",
                        expired
                ));
            }
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Heartbeat monitor database error", e);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "Heartbeat monitor error", e);
        }
    }

    @Override
    public void close() {
        started.set(false);
        scheduler.shutdownNow();
    }

    private static final class MonitorThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "tracker-heartbeat-monitor");
            thread.setDaemon(true);
            return thread;
        }
    }
}

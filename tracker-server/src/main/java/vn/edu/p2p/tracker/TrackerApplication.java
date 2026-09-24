package vn.edu.p2p.tracker;

import vn.edu.p2p.tracker.auth.BCryptPasswordService;
import vn.edu.p2p.tracker.auth.DefaultAuthService;
import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;
import vn.edu.p2p.tracker.config.TrackerSettings;
import vn.edu.p2p.tracker.network.TrackerRequestDispatcher;
import vn.edu.p2p.tracker.network.TrackerServer;
import vn.edu.p2p.tracker.peer.HeartbeatMonitor;
import vn.edu.p2p.tracker.peer.PeerSessionService;
import vn.edu.p2p.tracker.repository.PeerRepository;
import vn.edu.p2p.tracker.repository.PeerSessionRepository;
import vn.edu.p2p.tracker.repository.StatisticsRepository;
import vn.edu.p2p.tracker.repository.UserRepository;
import vn.edu.p2p.tracker.repository.jdbc.JdbcPeerRepository;
import vn.edu.p2p.tracker.repository.jdbc.JdbcPeerSessionRepository;
import vn.edu.p2p.tracker.repository.jdbc.JdbcStatisticsRepository;
import vn.edu.p2p.tracker.repository.jdbc.JdbcUserRepository;
import vn.edu.p2p.tracker.statistics.TrackerStatisticsMonitor;
import vn.edu.p2p.tracker.statistics.TrackerStatisticsService;

import java.time.Duration;

public final class TrackerApplication {
    private TrackerApplication() {
    }

    public static void main(String[] args) throws Exception {
        TrackerSettings settings = TrackerSettings.load();

        DatabaseConnectionFactory connectionFactory =
                new DatabaseConnectionFactory(settings.database());
        connectionFactory.verify();

        UserRepository users = new JdbcUserRepository(connectionFactory);
        PeerRepository peers = new JdbcPeerRepository(connectionFactory);
        PeerSessionRepository sessions =
                new JdbcPeerSessionRepository(connectionFactory);
        StatisticsRepository statistics =
                new JdbcStatisticsRepository(connectionFactory);

        DefaultAuthService authService = new DefaultAuthService(
                users,
                peers,
                sessions,
                new BCryptPasswordService(),
                settings.heartbeatIntervalSeconds()
        );
        PeerSessionService peerSessionService = new PeerSessionService(sessions);

        TrackerRequestDispatcher dispatcher =
                new TrackerRequestDispatcher(authService, peerSessionService);
        TrackerServer server = new TrackerServer(
                settings.trackerPort(),
                dispatcher
        );

        HeartbeatMonitor heartbeatMonitor = new HeartbeatMonitor(
                sessions,
                Duration.ofSeconds(settings.heartbeatTimeoutSeconds()),
                Duration.ofSeconds(settings.heartbeatIntervalSeconds())
        );
        TrackerStatisticsMonitor statisticsMonitor = new TrackerStatisticsMonitor(
                new TrackerStatisticsService(statistics),
                Duration.ofSeconds(settings.statisticsIntervalSeconds())
        );

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    statisticsMonitor.close();
                    heartbeatMonitor.close();
                    server.close();
                }, "tracker-shutdown")
        );

        System.out.println("==========================================");
        System.out.println(" FileShare-P2P Tracker — Week 4          ");
        System.out.println("==========================================");
        System.out.printf("Tracker port : %d%n", settings.trackerPort());
        System.out.printf("DB URL       : %s%n", settings.database().url());
        System.out.printf("DB user      : %s%n", settings.database().username());
        System.out.printf(
                "Heartbeat     : every %ds, timeout %ds%n",
                settings.heartbeatIntervalSeconds(),
                settings.heartbeatTimeoutSeconds()
        );
        System.out.printf(
                "Statistics    : every %ds%n",
                settings.statisticsIntervalSeconds()
        );
        System.out.println("DB check     : OK");
        System.out.println();

        heartbeatMonitor.start();
        statisticsMonitor.start();
        server.start();
    }
}

package vn.edu.p2p.tracker;

import vn.edu.p2p.tracker.config.TrackerSettings;

/**
 * Week 1 entry point.
 *
 * The real TCP accept loop and authentication implementation belong to Week 2.
 * This class verifies that the Tracker module and configuration are wired.
 */
public final class TrackerApplication {

    private TrackerApplication() {
    }

    public static void main(String[] args) {
        TrackerSettings settings = TrackerSettings.load();

        System.out.println("==========================================");
        System.out.println(" FileShare-P2P Tracker — Week 1 skeleton ");
        System.out.println("==========================================");
        System.out.printf("Tracker port : %d%n", settings.trackerPort());
        System.out.printf("DB URL       : %s%n", settings.database().url());
        System.out.printf("DB user      : %s%n", settings.database().username());
        System.out.printf(
                "Heartbeat    : every %ds, timeout %ds%n",
                settings.heartbeatIntervalSeconds(),
                settings.heartbeatTimeoutSeconds()
        );
        System.out.println();
        System.out.println(
                "Week 1 ready. TCP login + PostgreSQL authentication is implemented in Week 2."
        );
    }
}

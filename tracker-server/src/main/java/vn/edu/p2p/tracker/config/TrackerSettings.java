package vn.edu.p2p.tracker.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads local settings from classpath application.properties if present.
 * Environment variables override property values.
 *
 * application.properties is intentionally git-ignored.
 */
public record TrackerSettings(
        DatabaseConfig database,
        int trackerPort,
        int heartbeatIntervalSeconds,
        int heartbeatTimeoutSeconds
) {
    public static TrackerSettings load() {
        Properties properties = new Properties();

        try (InputStream input = TrackerSettings.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read application.properties", e);
        }

        String dbUrl = value(
                "DB_URL",
                properties,
                "db.url",
                "jdbc:postgresql://localhost:5432/fileshare_p2p"
        );
        String dbUsername = value(
                "DB_USERNAME",
                properties,
                "db.username",
                "postgres"
        );
        String dbPassword = value(
                "DB_PASSWORD",
                properties,
                "db.password",
                ""
        );

        int trackerPort = intValue(
                "TRACKER_PORT",
                properties,
                "tracker.port",
                9000
        );
        int heartbeatInterval = intValue(
                "HEARTBEAT_INTERVAL_SECONDS",
                properties,
                "heartbeat.interval.seconds",
                10
        );
        int heartbeatTimeout = intValue(
                "HEARTBEAT_TIMEOUT_SECONDS",
                properties,
                "heartbeat.timeout.seconds",
                30
        );

        if (heartbeatTimeout <= heartbeatInterval) {
            throw new IllegalStateException(
                    "heartbeat.timeout.seconds must be greater than heartbeat.interval.seconds"
            );
        }

        return new TrackerSettings(
                new DatabaseConfig(dbUrl, dbUsername, dbPassword),
                trackerPort,
                heartbeatInterval,
                heartbeatTimeout
        );
    }

    private static String value(
            String envKey,
            Properties properties,
            String propertyKey,
            String defaultValue
    ) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }

        String property = properties.getProperty(propertyKey);
        if (property != null && !property.isBlank()) {
            return property.trim();
        }

        return defaultValue;
    }

    private static int intValue(
            String envKey,
            Properties properties,
            String propertyKey,
            int defaultValue
    ) {
        String raw = value(
                envKey,
                properties,
                propertyKey,
                Integer.toString(defaultValue)
        );

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Invalid integer for " + propertyKey + ": " + raw,
                    e
            );
        }
    }
}

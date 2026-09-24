package vn.edu.p2p.tracker.config;

import vn.edu.p2p.common.config.LocalEnv;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Tracker settings using the following priority:
 *
 * 1. Operating-system environment variables
 * 2. Local .env file
 * 3. classpath application.properties
 * 4. safe/default development values
 *
 * .env and application.properties are local-only files and must not be committed.
 */
public record TrackerSettings(
        DatabaseConfig database,
        int trackerPort,
        int heartbeatIntervalSeconds,
        int heartbeatTimeoutSeconds,
        int statisticsIntervalSeconds
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

        LocalEnv dotenv = LocalEnv.load();

        String explicitDbUrl = optionalValue(
                "DB_URL",
                dotenv,
                properties,
                "db.url"
        );

        String dbUrl;
        if (explicitDbUrl != null) {
            dbUrl = explicitDbUrl;
        } else {
            String dbHost = value("DB_HOST", dotenv, properties, "db.host", "localhost");
            int dbPort = intValue("DB_PORT", dotenv, properties, "db.port", 5432);
            String dbName = value("DB_NAME", dotenv, properties, "db.name", "fileshare_p2p");
            dbUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
        }

        String dbUsername = firstNonBlank(
                env("DB_USER"),
                env("DB_USERNAME"),
                dotenv.get("DB_USER"),
                dotenv.get("DB_USERNAME"),
                properties.getProperty("db.user"),
                properties.getProperty("db.username"),
                "postgres"
        );

        String dbPassword = value(
                "DB_PASSWORD",
                dotenv,
                properties,
                "db.password",
                ""
        );

        if (dbPassword.isBlank()) {
            throw new IllegalStateException(
                    "Database password is empty. Set DB_PASSWORD in .env/environment "
                            + "or db.password in application.properties."
            );
        }

        int trackerPort = intValue(
                "TRACKER_PORT",
                dotenv,
                properties,
                "tracker.port",
                9000
        );
        int heartbeatInterval = intValue(
                "HEARTBEAT_INTERVAL_SECONDS",
                dotenv,
                properties,
                "heartbeat.interval.seconds",
                10
        );
        int heartbeatTimeout = intValue(
                "HEARTBEAT_TIMEOUT_SECONDS",
                dotenv,
                properties,
                "heartbeat.timeout.seconds",
                30
        );
        int statisticsInterval = intValue(
                "STATISTICS_INTERVAL_SECONDS",
                dotenv,
                properties,
                "statistics.interval.seconds",
                30
        );

        if (heartbeatTimeout <= heartbeatInterval) {
            throw new IllegalStateException(
                    "heartbeat.timeout.seconds must be greater than heartbeat.interval.seconds"
            );
        }

        if (statisticsInterval < 1) {
            throw new IllegalStateException(
                    "statistics.interval.seconds must be positive"
            );
        }

        return new TrackerSettings(
                new DatabaseConfig(dbUrl, dbUsername, dbPassword),
                trackerPort,
                heartbeatInterval,
                heartbeatTimeout,
                statisticsInterval
        );
    }

    private static String optionalValue(
            String envKey,
            LocalEnv dotenv,
            Properties properties,
            String propertyKey
    ) {
        return firstNonBlank(
                env(envKey),
                dotenv.get(envKey),
                properties.getProperty(propertyKey)
        );
    }

    private static String value(
            String envKey,
            LocalEnv dotenv,
            Properties properties,
            String propertyKey,
            String defaultValue
    ) {
        String result = firstNonBlank(
                env(envKey),
                dotenv.get(envKey),
                properties.getProperty(propertyKey)
        );
        return result == null ? defaultValue : result;
    }

    private static int intValue(
            String envKey,
            LocalEnv dotenv,
            Properties properties,
            String propertyKey,
            int defaultValue
    ) {
        String raw = value(
                envKey,
                dotenv,
                properties,
                propertyKey,
                Integer.toString(defaultValue)
        );

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "Invalid integer for " + envKey + "/" + propertyKey + ": " + raw,
                    e
            );
        }
    }

    private static String env(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }
}

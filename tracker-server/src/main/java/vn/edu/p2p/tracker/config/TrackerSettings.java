package vn.edu.p2p.tracker.config;

import vn.edu.p2p.common.config.LocalEnv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Tracker configuration priority:
 *
 * 1. OS environment variables
 * 2. project .env
 * 3. classpath application.properties
 * 4. built-in defaults
 */
public record TrackerSettings(
        DatabaseConfig database,
        int trackerPort,
        int heartbeatIntervalSeconds,
        int heartbeatTimeoutSeconds,
        int statisticsIntervalSeconds,
        int workerThreads,
        int workerQueueCapacity,
        int socketReadTimeoutMillis,
        String logLevel,
        TlsConfig tls
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
                "DB_URL", dotenv, properties, "db.url"
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
                "DB_PASSWORD", dotenv, properties, "db.password", ""
        );

        if (dbPassword.isBlank()) {
            throw new IllegalStateException(
                    "Database password is empty. Set DB_PASSWORD in .env/environment "
                            + "or db.password in application.properties."
            );
        }

        int trackerPort = intValue(
                "TRACKER_PORT", dotenv, properties, "tracker.port", 9000
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
        int workerThreads = intValue(
                "TRACKER_WORKER_THREADS",
                dotenv,
                properties,
                "tracker.worker.threads",
                16
        );
        int workerQueueCapacity = intValue(
                "TRACKER_WORKER_QUEUE_CAPACITY",
                dotenv,
                properties,
                "tracker.worker.queue.capacity",
                128
        );
        int socketReadTimeoutMillis = intValue(
                "TRACKER_SOCKET_READ_TIMEOUT_MILLIS",
                dotenv,
                properties,
                "tracker.socket.read.timeout.millis",
                30_000
        );
        String logLevel = value(
                "TRACKER_LOG_LEVEL",
                dotenv,
                properties,
                "tracker.log.level",
                "INFO"
        );

        boolean tlsEnabled = booleanValue(
                "TRACKER_TLS_ENABLED",
                dotenv,
                properties,
                "tracker.tls.enabled",
                false
        );

        Path keyStorePath = dotenv.resolvePath(
                value(
                        "TRACKER_TLS_KEYSTORE",
                        dotenv,
                        properties,
                        "tracker.tls.keystore.path",
                        "certs/tracker-server.p12"
                )
        );

        String keyStorePassword = value(
                "TRACKER_TLS_KEYSTORE_PASSWORD",
                dotenv,
                properties,
                "tracker.tls.keystore.password",
                ""
        );

        String keyStoreType = value(
                "TRACKER_TLS_KEYSTORE_TYPE",
                dotenv,
                properties,
                "tracker.tls.keystore.type",
                "PKCS12"
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
        if (workerThreads < 1 || workerQueueCapacity < 1 || socketReadTimeoutMillis < 1) {
            throw new IllegalStateException(
                    "Tracker worker/queue/socket timeout settings must be positive"
            );
        }

        return new TrackerSettings(
                new DatabaseConfig(dbUrl, dbUsername, dbPassword),
                trackerPort,
                heartbeatInterval,
                heartbeatTimeout,
                statisticsInterval,
                workerThreads,
                workerQueueCapacity,
                socketReadTimeoutMillis,
                logLevel,
                new TlsConfig(
                        tlsEnabled,
                        keyStorePath,
                        keyStorePassword,
                        keyStoreType
                )
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

    private static boolean booleanValue(
            String envKey,
            LocalEnv dotenv,
            Properties properties,
            String propertyKey,
            boolean defaultValue
    ) {
        String raw = value(
                envKey,
                dotenv,
                properties,
                propertyKey,
                Boolean.toString(defaultValue)
        );

        if ("true".equalsIgnoreCase(raw)) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw)) {
            return false;
        }

        throw new IllegalStateException(
                "Invalid boolean for " + envKey + "/" + propertyKey + ": " + raw
        );
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

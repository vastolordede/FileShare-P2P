package vn.edu.p2p.peer.network;

import vn.edu.p2p.common.config.LocalEnv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Peer configuration priority:
 *
 * 1. OS environment variables
 * 2. project .env
 * 3. classpath application.properties
 * 4. built-in defaults
 */
public record PeerSettings(
        String trackerHost,
        int trackerPort,
        boolean trackerTlsEnabled,
        Path trackerTrustStorePath,
        String trackerTrustStorePassword,
        String trackerTrustStoreType
) {
    public static PeerSettings load() {
        Properties properties = new Properties();

        try (InputStream input = PeerSettings.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot read Peer application.properties",
                    e
            );
        }

        LocalEnv dotenv = LocalEnv.load();

        String host = value(
                "TRACKER_HOST", dotenv, properties, "tracker.host", "127.0.0.1"
        );
        int port = intValue(
                "TRACKER_PORT", dotenv, properties, "tracker.port", 9000
        );
        boolean tlsEnabled = booleanValue(
                "TRACKER_TLS_ENABLED",
                dotenv,
                properties,
                "tracker.tls.enabled",
                false
        );

        Path trustStorePath = dotenv.resolvePath(
                value(
                        "TRACKER_TRUSTSTORE",
                        dotenv,
                        properties,
                        "tracker.tls.truststore.path",
                        "certs/peer-truststore.p12"
                )
        );

        String trustStorePassword = value(
                "TRACKER_TRUSTSTORE_PASSWORD",
                dotenv,
                properties,
                "tracker.tls.truststore.password",
                ""
        );

        String trustStoreType = value(
                "TRACKER_TRUSTSTORE_TYPE",
                dotenv,
                properties,
                "tracker.tls.truststore.type",
                "PKCS12"
        );

        return new PeerSettings(
                host,
                port,
                tlsEnabled,
                trustStorePath,
                trustStorePassword,
                trustStoreType
        );
    }

    public TrackerConnectionConfig trackerConnectionConfig() {
        return new TrackerConnectionConfig(
                trackerHost,
                trackerPort,
                trackerTlsEnabled,
                trackerTrustStorePath,
                trackerTrustStorePassword,
                trackerTrustStoreType
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

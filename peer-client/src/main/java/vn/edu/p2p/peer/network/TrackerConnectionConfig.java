package vn.edu.p2p.peer.network;

import java.nio.file.Path;

public record TrackerConnectionConfig(
        String host,
        int port,
        boolean tlsEnabled,
        Path trustStorePath,
        String trustStorePassword,
        String trustStoreType
) {
    public TrackerConnectionConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        if (tlsEnabled) {
            if (trustStorePath == null) {
                throw new IllegalArgumentException(
                        "trustStorePath is required when Tracker TLS is enabled"
                );
            }
            if (trustStorePassword == null || trustStorePassword.isBlank()) {
                throw new IllegalArgumentException(
                        "trustStorePassword is required when Tracker TLS is enabled"
                );
            }
        }
    }
}

package vn.edu.p2p.peer.network;

/**
 * Week 1 immutable connection configuration.
 * Actual Socket/SSLSocket connection belongs to Week 2.
 */
public record TrackerConnectionConfig(
        String host,
        int port
) {
    public TrackerConnectionConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
    }
}

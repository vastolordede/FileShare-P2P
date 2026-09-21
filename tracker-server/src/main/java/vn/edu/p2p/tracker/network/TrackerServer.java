package vn.edu.p2p.tracker.network;

/**
 * Week 1 architectural placeholder.
 *
 * Week 2 responsibility:
 * - bind ServerSocket / SSLServerSocket
 * - accept Peer connections
 * - hand connections to a bounded ExecutorService
 * - decode MessageEnvelope via ProtocolCodec
 * - dispatch LOGIN/LOGOUT/HEARTBEAT requests
 */
public final class TrackerServer {
    private final int port;

    public TrackerServer(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid TCP port: " + port);
        }
        this.port = port;
    }

    public int port() {
        return port;
    }
}

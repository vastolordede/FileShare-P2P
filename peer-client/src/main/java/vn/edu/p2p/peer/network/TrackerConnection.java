package vn.edu.p2p.peer.network;

import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.ProtocolCodec;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public final class TrackerConnection implements AutoCloseable {
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;

    private final TrackerConnectionConfig config;
    private Socket socket;

    public TrackerConnection(TrackerConnectionConfig config) {
        this.config = config;
    }

    public synchronized MessageEnvelope request(MessageEnvelope request)
            throws IOException {
        ensureConnected();

        try {
            ProtocolCodec.write(socket.getOutputStream(), request);
            MessageEnvelope response = ProtocolCodec.read(socket.getInputStream());

            if (!request.requestId().equals(response.requestId())) {
                throw new IOException(
                        "Tracker response requestId mismatch: " + response.requestId()
                );
            }
            return response;
        } catch (IOException e) {
            closeSocket();
            throw e;
        }
    }

    private void ensureConnected() throws IOException {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        Socket candidate = new Socket();
        candidate.connect(
                new InetSocketAddress(config.host(), config.port()),
                CONNECT_TIMEOUT_MILLIS
        );
        candidate.setSoTimeout(READ_TIMEOUT_MILLIS);
        candidate.setTcpNoDelay(true);
        socket = candidate;
    }

    @Override
    public synchronized void close() {
        closeSocket();
    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // best effort
            }
            socket = null;
        }
    }
}

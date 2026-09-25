package vn.edu.p2p.tracker.network;

import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.ProtocolCodec;
import vn.edu.p2p.common.protocol.ProtocolException;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ClientHandler implements Runnable {
    private static final Logger LOG = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final TrackerRequestDispatcher dispatcher;
    private final int readTimeoutMillis;

    ClientHandler(
            Socket socket,
            TrackerRequestDispatcher dispatcher,
            int readTimeoutMillis
    ) {
        this.socket = socket;
        this.dispatcher = dispatcher;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    @Override
    public void run() {
        String remoteIp = socket.getInetAddress().getHostAddress();

        try (Socket client = socket) {
            client.setTcpNoDelay(true);
            client.setSoTimeout(readTimeoutMillis);

            while (!client.isClosed()) {
                final MessageEnvelope request;
                try {
                    request = ProtocolCodec.read(client.getInputStream());
                } catch (EOFException e) {
                    break;
                } catch (SocketTimeoutException e) {
                    LOG.info(() -> String.format(
                            "Peer connection %s timed out after %d ms",
                            remoteIp,
                            readTimeoutMillis
                    ));
                    break;
                } catch (ProtocolException e) {
                    writeProtocolError(client, e);
                    break;
                }

                MessageEnvelope response = dispatcher.dispatch(request, remoteIp);
                ProtocolCodec.write(client.getOutputStream(), response);
            }
        } catch (IOException e) {
            LOG.log(
                    Level.FINE,
                    "Peer connection " + remoteIp + " closed: " + e.getMessage(),
                    e
            );
        }
    }

    private static void writeProtocolError(Socket client, ProtocolException error) {
        try {
            ProtocolCodec.write(
                    client.getOutputStream(),
                    TrackerErrorResponses.protocolError(
                            "unknown",
                            "INVALID_PROTOCOL_MESSAGE",
                            error.getMessage()
                    )
            );
        } catch (IOException ignored) {
            // The connection is already invalid; close it best-effort.
        }
    }
}

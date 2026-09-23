package vn.edu.p2p.tracker.network;

import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.ProtocolCodec;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

final class ClientHandler implements Runnable {
    private final Socket socket;
    private final TrackerRequestDispatcher dispatcher;

    ClientHandler(Socket socket, TrackerRequestDispatcher dispatcher) {
        this.socket = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        String remoteIp = socket.getInetAddress().getHostAddress();

        try (Socket client = socket) {
            client.setTcpNoDelay(true);

            while (!client.isClosed()) {
                final MessageEnvelope request;
                try {
                    request = ProtocolCodec.read(client.getInputStream());
                } catch (EOFException e) {
                    break;
                }

                MessageEnvelope response = dispatcher.dispatch(request, remoteIp);
                ProtocolCodec.write(client.getOutputStream(), response);
            }
        } catch (IOException e) {
            System.err.printf(
                    "Peer connection %s closed: %s%n",
                    remoteIp,
                    e.getMessage()
            );
        }
    }
}

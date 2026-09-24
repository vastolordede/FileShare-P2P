package vn.edu.p2p.tracker.network;

import vn.edu.p2p.common.security.TlsContextFactory;
import vn.edu.p2p.tracker.config.TlsConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.GeneralSecurityException;

public final class TlsTrackerServerSocketProvider
        implements TrackerServerSocketProvider {

    private final SSLContext sslContext;

    public TlsTrackerServerSocketProvider(TlsConfig config) {
        try {
            this.sslContext = TlsContextFactory.createServerContext(
                    config.keyStorePath(),
                    config.keyStorePassword(),
                    config.keyStoreType()
            );
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Cannot initialize Tracker TLS context",
                    e
            );
        }
    }

    @Override
    public ServerSocket open(int port) throws IOException {
        SSLServerSocket socket = (SSLServerSocket)
                sslContext.getServerSocketFactory().createServerSocket(port);

        socket.setEnabledProtocols(
                supportedProtocols(socket.getSupportedProtocols())
        );
        socket.setNeedClientAuth(false);
        return socket;
    }

    private static String[] supportedProtocols(String[] supported) {
        java.util.List<String> preferred = new java.util.ArrayList<>();
        java.util.Set<String> available = java.util.Set.of(supported);

        if (available.contains("TLSv1.3")) {
            preferred.add("TLSv1.3");
        }
        if (available.contains("TLSv1.2")) {
            preferred.add("TLSv1.2");
        }

        if (preferred.isEmpty()) {
            throw new IllegalStateException(
                    "JVM does not support TLSv1.2 or TLSv1.3"
            );
        }
        return preferred.toArray(String[]::new);
    }
}

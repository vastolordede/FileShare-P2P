package vn.edu.p2p.peer.transfer;

import java.nio.file.Path;

/**
 * S07-S08: Cấu hình kết nối trực tiếp Peer <-> Peer (hỗ trợ TCP và TLS).
 */
public record PeerP2pSocketConfig(
        boolean tlsEnabled,
        Path keyStorePath,
        String keyStorePassword,
        String keyStoreType,
        Path trustStorePath,
        String trustStorePassword,
        String trustStoreType,
        int connectTimeoutMillis,
        int readTimeoutMillis
) {
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5_000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 15_000;

    public static PeerP2pSocketConfig plainTcp() {
        return new PeerP2pSocketConfig(
                false, null, null, "PKCS12",
                null, null, "PKCS12",
                DEFAULT_CONNECT_TIMEOUT_MS,
                DEFAULT_READ_TIMEOUT_MS
        );
    }

    public static PeerP2pSocketConfig tls(
            Path keyStorePath,
            String keyStorePassword,
            Path trustStorePath,
            String trustStorePassword
    ) {
        return new PeerP2pSocketConfig(
                true,
                keyStorePath,
                keyStorePassword,
                "PKCS12",
                trustStorePath,
                trustStorePassword,
                "PKCS12",
                DEFAULT_CONNECT_TIMEOUT_MS,
                DEFAULT_READ_TIMEOUT_MS
        );
    }
}
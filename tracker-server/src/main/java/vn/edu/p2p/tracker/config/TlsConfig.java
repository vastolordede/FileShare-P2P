package vn.edu.p2p.tracker.config;

import java.nio.file.Path;

public record TlsConfig(
        boolean enabled,
        Path keyStorePath,
        String keyStorePassword,
        String keyStoreType
) {
    public TlsConfig {
        if (enabled) {
            if (keyStorePath == null) {
                throw new IllegalArgumentException(
                        "TLS keystore path is required when TLS is enabled"
                );
            }
            if (keyStorePassword == null || keyStorePassword.isBlank()) {
                throw new IllegalArgumentException(
                        "TLS keystore password is required when TLS is enabled"
                );
            }
        }
    }
}

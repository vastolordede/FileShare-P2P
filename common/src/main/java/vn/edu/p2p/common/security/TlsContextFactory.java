package vn.edu.p2p.common.security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

public final class TlsContextFactory {
    private TlsContextFactory() {
    }

    public static SSLContext createServerContext(
            Path keyStorePath,
            String keyStorePassword,
            String keyStoreType
    ) throws IOException, GeneralSecurityException {
        KeyStore keyStore = loadStore(
                keyStorePath,
                keyStorePassword,
                keyStoreType
        );

        KeyManagerFactory keyManagers = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
        );
        keyManagers.init(keyStore, keyStorePassword.toCharArray());

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManagers.getKeyManagers(), null, null);
        return context;
    }

    public static SSLContext createClientContext(
            Path trustStorePath,
            String trustStorePassword,
            String trustStoreType
    ) throws IOException, GeneralSecurityException {
        KeyStore trustStore = loadStore(
                trustStorePath,
                trustStorePassword,
                trustStoreType
        );

        TrustManagerFactory trustManagers = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
        );
        trustManagers.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustManagers.getTrustManagers(), null);
        return context;
    }

    private static KeyStore loadStore(
            Path path,
            String password,
            String type
    ) throws IOException, GeneralSecurityException {
        if (path == null) {
            throw new IllegalArgumentException("TLS store path is required");
        }
        if (!Files.isRegularFile(path)) {
            throw new IOException("TLS store does not exist: " + path);
        }

        String storeType = type == null || type.isBlank()
                ? "PKCS12"
                : type.trim();

        KeyStore store = KeyStore.getInstance(storeType);
        try (InputStream input = Files.newInputStream(path)) {
            store.load(input, password.toCharArray());
        }
        return store;
    }
}

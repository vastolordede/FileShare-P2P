package vn.edu.p2p.peer.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;

public final class PeerIdentityStore {
    private static final String PEER_ID = "peer.id";
    private static final String DEVICE_NAME = "device.name";

    private final Path file;

    public PeerIdentityStore(Path file) {
        this.file = file;
    }

    public static PeerIdentityStore defaultStore() {
        Path file = Path.of(
                System.getProperty("user.home"),
                ".fileshare-p2p",
                "peer.properties"
        );
        return new PeerIdentityStore(file);
    }

    public PeerIdentity loadOrCreate() throws IOException {
        if (Files.exists(file)) {
            return load();
        }

        PeerIdentity identity = new PeerIdentity(
                UUID.randomUUID().toString(),
                detectDeviceName()
        );
        save(identity);
        return identity;
    }

    private PeerIdentity load() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
        }

        String peerId = properties.getProperty(PEER_ID);
        String deviceName = properties.getProperty(DEVICE_NAME, detectDeviceName());

        try {
            UUID.fromString(peerId);
        } catch (Exception e) {
            throw new IOException("Invalid local peer identity: " + file, e);
        }

        return new PeerIdentity(peerId, deviceName);
    }

    private void save(PeerIdentity identity) throws IOException {
        Files.createDirectories(file.getParent());

        Properties properties = new Properties();
        properties.setProperty(PEER_ID, identity.peerId());
        properties.setProperty(DEVICE_NAME, identity.deviceName());

        try (OutputStream output = Files.newOutputStream(file)) {
            properties.store(output, "FileShare-P2P local peer identity");
        }
    }

    private static String detectDeviceName() {
        try {
            String host = InetAddress.getLocalHost().getHostName();
            return host == null || host.isBlank() ? "Unknown device" : host;
        } catch (Exception e) {
            return "Unknown device";
        }
    }
}

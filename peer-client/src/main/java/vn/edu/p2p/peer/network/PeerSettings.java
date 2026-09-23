package vn.edu.p2p.peer.network;

public record PeerSettings(String trackerHost, int trackerPort) {
    public static PeerSettings load() {
        String host = envOrDefault("TRACKER_HOST", "127.0.0.1");
        int port = intEnvOrDefault("TRACKER_PORT", 9000);
        return new PeerSettings(host, port);
    }

    public TrackerConnectionConfig trackerConnectionConfig() {
        return new TrackerConnectionConfig(trackerHost, trackerPort);
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static int intEnvOrDefault(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer environment variable " + key, e);
        }
    }
}

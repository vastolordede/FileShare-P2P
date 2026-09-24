package vn.edu.p2p.common.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal .env loader for local development.
 *
 * Priority is handled by callers:
 * System environment > .env > application.properties > defaults.
 *
 * No external dependency is required.
 */
public final class LocalEnv {
    private final Map<String, String> values;
    private final Path source;

    private LocalEnv(Map<String, String> values, Path source) {
        this.values = Map.copyOf(values);
        this.source = source;
    }

    public static LocalEnv load() {
        Path envFile = findEnvFile();
        if (envFile == null) {
            return new LocalEnv(Map.of(), null);
        }

        try {
            return new LocalEnv(parse(Files.readAllLines(envFile, StandardCharsets.UTF_8)), envFile);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read .env file: " + envFile.toAbsolutePath(), e);
        }
    }

    public String get(String key) {
        return values.get(key);
    }

    public Path source() {
        return source;
    }

    private static Path findEnvFile() {
        String explicit = System.getenv("P2P_ENV_FILE");
        if (explicit != null && !explicit.isBlank()) {
            Path explicitPath = Path.of(explicit.trim()).toAbsolutePath().normalize();
            if (!Files.isRegularFile(explicitPath)) {
                throw new IllegalStateException(
                        "P2P_ENV_FILE points to a missing file: " + explicitPath
                );
            }
            return explicitPath;
        }

        List<Path> candidates = List.of(
                Path.of(".env"),
                Path.of("..", ".env"),
                Path.of("tracker-server", ".env")
        );

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    private static Map<String, String> parse(List<String> lines) {
        Map<String, String> result = new LinkedHashMap<>();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            if (line.startsWith("export ")) {
                line = line.substring("export ".length()).trim();
            }

            int separator = line.indexOf('=');
            if (separator <= 0) {
                continue;
            }

            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();

            if (value.length() >= 2) {
                char first = value.charAt(0);
                char last = value.charAt(value.length() - 1);
                if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                    value = value.substring(1, value.length() - 1);
                }
            }

            if (!key.isBlank()) {
                result.put(key, value);
            }
        }

        return result;
    }
}

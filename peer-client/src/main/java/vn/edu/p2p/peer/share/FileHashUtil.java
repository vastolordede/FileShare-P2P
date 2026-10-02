package vn.edu.p2p.peer.share;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hashing helpers used to build FileMetadata / PieceInfo.
 *
 * Hex output is lowercase, matching the DB check constraint
 * ck_files_sha256: file_hash ~ '^[0-9A-Fa-f]{64}$'.
 */
public final class FileHashUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    private FileHashUtil() {
    }

    /** Hash of the whole file, streamed (safe for large files). */
    public static String sha256(Path file) throws IOException {
        MessageDigest digest = newDigest();

        try (InputStream input = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return toHex(digest.digest());
    }

    /** Hash of a single in-memory piece (buffer[offset..offset+length)). */
    public static String sha256(byte[] data, int offset, int length) {
        MessageDigest digest = newDigest();
        digest.update(data, offset, length);
        return toHex(digest.digest());
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every standard JVM.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}

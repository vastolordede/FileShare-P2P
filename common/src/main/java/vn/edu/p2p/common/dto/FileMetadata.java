package vn.edu.p2p.common.dto;

import java.util.List;

/**
 * Full metadata of a shared file, built locally by the sharing Peer
 * before it is published to the Tracker (see FileMetadataBuilder).
 *
 * Mirrors the files table: file_name, file_size, file_hash (SHA-256),
 * piece_size, piece_count, plus the per-piece list (file_pieces table).
 */
public record FileMetadata(
        String fileName,
        long fileSize,
        String fileHash,
        int pieceSize,
        int pieceCount,
        List<PieceInfo> pieces
) {
    public FileMetadata {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be blank");
        }
        if (fileSize < 0) {
            throw new IllegalArgumentException("fileSize must be >= 0");
        }
        if (fileHash == null || !fileHash.matches("^[0-9A-Fa-f]{64}$")) {
            throw new IllegalArgumentException("fileHash must be a 64-char SHA-256 hex string");
        }
        if (pieceSize <= 0) {
            throw new IllegalArgumentException("pieceSize must be > 0");
        }
        pieces = pieces == null ? List.of() : List.copyOf(pieces);
    }
}

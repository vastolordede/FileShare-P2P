package vn.edu.p2p.common.dto;

/**
 * Metadata of a single piece/chunk of a file.
 * Mirrors the file_pieces table (file_id, piece_index, piece_length, piece_hash).
 */
public record PieceInfo(
        int pieceIndex,
        int pieceLength,
        String pieceHash
) {
}

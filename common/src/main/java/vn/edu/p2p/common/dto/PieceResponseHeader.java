package vn.edu.p2p.common.dto;

/**
 * S04: JSON Header của bản tin PIECE_RESPONSE.
 * Ngay sau JSON Header này trên luồng Socket sẽ là đúng `pieceLength` byte nhị phân (S05).
 */
public record PieceResponseHeader(
        long fileId,
        int pieceIndex,
        int pieceLength,
        String pieceHash
) {
    public PieceResponseHeader {
        if (fileId <= 0) {
            throw new IllegalArgumentException("fileId must be > 0");
        }
        if (pieceIndex < 0) {
            throw new IllegalArgumentException("pieceIndex must be >= 0");
        }
        if (pieceLength < 0) {
            throw new IllegalArgumentException("pieceLength must be >= 0");
        }
    }
}
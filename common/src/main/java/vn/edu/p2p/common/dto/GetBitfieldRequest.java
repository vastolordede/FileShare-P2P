package vn.edu.p2p.common.dto;

/**
 * S21: Request gửi trực tiếp từ Downloader Peer tới Uploader Peer
 * để hỏi xem Uploader đang có những piece nào của fileId.
 */
public record GetBitfieldRequest(long fileId) {
    public GetBitfieldRequest {
        if (fileId <= 0) {
            throw new IllegalArgumentException("fileId must be > 0");
        }
    }
}
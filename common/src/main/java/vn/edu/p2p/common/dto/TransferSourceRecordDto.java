package vn.edu.p2p.common.dto;

/**
 * S36: Thông tin một Peer nguồn tham gia vào phiên tải file
 * (ánh xạ trực tiếp vào bảng transfer_sources).
 */
public record TransferSourceRecordDto(
        String sourcePeerId,
        long bytesReceived,
        int piecesReceived,
        String sourceStatus,
        long connectedAtEpochMillis,
        Long disconnectedAtEpochMillis
) {
}
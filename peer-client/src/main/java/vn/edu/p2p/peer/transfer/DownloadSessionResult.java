package vn.edu.p2p.peer.transfer;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * S35-S37: Kết quả tổng hợp của một phiên tải file (khớp với bảng transfer_sessions
 * và transfer_sources trong PostgreSQL).
 */
public record DownloadSessionResult(
        UUID transferId,
        long fileId,
        Path outputFile,
        String status,
        long bytesReceived,
        Instant startedAt,
        Instant completedAt,
        String failureReason,
        List<SourceTransferStat> sourceStats
) {
    public record SourceTransferStat(
            String sourcePeerId,
            long bytesReceived,
            int piecesReceived,
            String sourceStatus,
            Instant connectedAt,
            Instant disconnectedAt
    ) {
    }
}
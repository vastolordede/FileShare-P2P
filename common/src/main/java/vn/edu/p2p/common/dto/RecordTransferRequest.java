package vn.edu.p2p.common.dto;

import java.util.List;

/**
 * S35-S37: Bản tin từ Peer gửi lên Tracker khi bắt đầu tải (DOWNLOADING)
 * hoặc khi kết thúc phiên tải (COMPLETED / FAILED / CANCELLED).
 */
public record RecordTransferRequest(
        String sessionId,
        String transferId,
        long fileId,
        String status,
        long bytesReceived,
        long startedAtEpochMillis,
        Long completedAtEpochMillis,
        String failureReason,
        List<TransferSourceRecordDto> sources
) {
    public RecordTransferRequest {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
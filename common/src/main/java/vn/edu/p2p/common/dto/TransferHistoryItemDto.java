package vn.edu.p2p.common.dto;

/**
 * S38: Một dòng lịch sử truyền file hiển thị trên màn hình Transfer History.
 */
public record TransferHistoryItemDto(
        String transferId,
        long fileId,
        String fileName,
        long fileSize,
        String status,
        long bytesReceived,
        long startedAtEpochMillis,
        Long completedAtEpochMillis,
        String failureReason,
        int sourceCount
) {
}
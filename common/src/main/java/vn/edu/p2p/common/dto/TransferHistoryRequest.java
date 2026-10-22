package vn.edu.p2p.common.dto;

/**
 * S38: Yêu cầu lấy danh sách lịch sử tải file của Peer hiện tại.
 */
public record TransferHistoryRequest(String sessionId) {
}
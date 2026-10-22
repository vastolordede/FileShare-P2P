package vn.edu.p2p.common.dto;

import java.util.List;

public record TransferHistoryResponse(List<TransferHistoryItemDto> items) {
    public TransferHistoryResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
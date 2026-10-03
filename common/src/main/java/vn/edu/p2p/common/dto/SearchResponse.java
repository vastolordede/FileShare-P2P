package vn.edu.p2p.common.dto;

import java.util.List;

public record SearchResponse(
        List<SearchResultItem> results
) {
    public SearchResponse {
        results = results == null ? List.of() : List.copyOf(results);
    }
}

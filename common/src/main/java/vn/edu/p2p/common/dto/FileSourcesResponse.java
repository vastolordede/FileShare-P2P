package vn.edu.p2p.common.dto;

import java.util.List;

public record FileSourcesResponse(
        long fileId,
        List<FileSourceInfo> sources
) {
    public FileSourcesResponse {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}

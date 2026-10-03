package vn.edu.p2p.common.dto;

/** Search files by (partial) name (T16). */
public record SearchRequest(
        String sessionId,
        String query
) {
}

package vn.edu.p2p.common.dto;

/**
 * Tracker's reply to ShareFileRequest.
 *
 * isNewFile = false means the SHA-256 already existed in the catalog
 * (T13 duplicate detection) and this file_id was reused rather than
 * inserted again.
 */
public record ShareFileResponse(
        long fileId,
        boolean isNewFile,
        String availabilityStatus
) {
}

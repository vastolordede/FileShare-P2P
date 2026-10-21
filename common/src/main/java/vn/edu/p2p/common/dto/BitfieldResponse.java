package vn.edu.p2p.common.dto;

import java.util.BitSet;
import java.util.Objects;

/**
 * S22: Phản hồi BITFIELD từ Peer nguồn trả về cho Downloader.
 * Chứa mảng byte biểu diễn BitSet các mảnh hiện có và FileMetadata của file.
 */
public record BitfieldResponse(
        long fileId,
        byte[] bitfieldBytes,
        FileMetadata metadata
) {
    public BitfieldResponse {
        if (fileId <= 0) {
            throw new IllegalArgumentException("fileId must be > 0");
        }
        Objects.requireNonNull(metadata, "metadata must not be null");
        bitfieldBytes = bitfieldBytes == null ? new byte[0] : bitfieldBytes.clone();
    }

    public BitSet toBitSet() {
        return BitSet.valueOf(bitfieldBytes);
    }
}
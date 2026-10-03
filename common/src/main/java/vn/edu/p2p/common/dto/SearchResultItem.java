package vn.edu.p2p.common.dto;

/** One row of a search result: file identity plus how many peers currently share it. */
public record SearchResultItem(
        long fileId,
        String fileName,
        long fileSize,
        String fileHash,
        int sourceCount
) {
}

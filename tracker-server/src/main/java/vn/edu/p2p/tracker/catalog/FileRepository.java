package vn.edu.p2p.tracker.catalog;

import vn.edu.p2p.common.dto.FileMetadata;

import java.sql.SQLException;
import java.util.Optional;

public interface FileRepository {

    /** T13: duplicate detection - find an existing file by its SHA-256 hash. */
    Optional<Long> findFileIdByHash(String fileHash) throws SQLException;

    /** T11-T12: insert a new file plus its pieces (files, file_pieces). Returns the new file_id. */
    long insert(FileMetadata metadata) throws SQLException;
}

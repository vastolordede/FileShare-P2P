package vn.edu.p2p.tracker.repository;

import vn.edu.p2p.tracker.domain.FileSourceRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface FileSourceRepository {
    List<FileSourceRecord> findOnlineSources(
            long fileId,
            UUID excludingPeerId
    ) throws SQLException;
}

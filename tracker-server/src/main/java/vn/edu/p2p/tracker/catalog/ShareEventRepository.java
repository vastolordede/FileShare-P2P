package vn.edu.p2p.tracker.catalog;

import java.sql.SQLException;
import java.util.UUID;

public interface ShareEventRepository {

    /** T24-T25: append a SHARE_START or SHARE_STOP row into share_events. */
    void insert(UUID peerId, long fileId, String eventType) throws SQLException;
}

package vn.edu.p2p.tracker.catalog;

import java.sql.SQLException;
import java.util.UUID;

public interface PeerFileRepository {

    /**
     * T14-T15: record that a peer owns/shares a file (peer_files table),
     * or refresh its status if the (peer_id, file_id) row already exists.
     */
    void upsertOwnership(UUID peerId, long fileId, String availabilityStatus) throws SQLException;
}

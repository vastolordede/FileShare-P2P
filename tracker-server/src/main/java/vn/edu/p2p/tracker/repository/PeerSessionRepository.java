package vn.edu.p2p.tracker.repository;

import vn.edu.p2p.tracker.domain.PeerSessionRecord;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PeerSessionRepository {
    void create(PeerSessionRecord session) throws SQLException;

    Optional<PeerSessionRecord> findBySessionId(UUID sessionId) throws SQLException;

    void closeActiveForPeer(UUID peerId, OffsetDateTime closedAt) throws SQLException;

    boolean updateLastSeen(UUID sessionId, OffsetDateTime lastSeen) throws SQLException;

    boolean markLoggedOut(UUID sessionId, OffsetDateTime logoutAt) throws SQLException;
}

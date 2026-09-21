package vn.edu.p2p.tracker.repository;

import vn.edu.p2p.tracker.domain.PeerSessionRecord;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Contract only for Week 1.
 * JDBC implementation is part of Week 2.
 */
public interface PeerSessionRepository {
    void create(PeerSessionRecord session) throws SQLException;

    Optional<PeerSessionRecord> findBySessionId(UUID sessionId) throws SQLException;

    void updateLastSeen(UUID sessionId, OffsetDateTime lastSeen) throws SQLException;

    void markOffline(UUID sessionId, OffsetDateTime logoutAt) throws SQLException;
}

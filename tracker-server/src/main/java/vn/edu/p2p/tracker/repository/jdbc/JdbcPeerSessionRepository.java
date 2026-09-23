package vn.edu.p2p.tracker.repository.jdbc;

import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;
import vn.edu.p2p.tracker.domain.PeerSessionRecord;
import vn.edu.p2p.tracker.domain.PeerStatus;
import vn.edu.p2p.tracker.repository.PeerSessionRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

public final class JdbcPeerSessionRepository implements PeerSessionRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public JdbcPeerSessionRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void create(PeerSessionRecord session) throws SQLException {
        String sql = """
                INSERT INTO peer_sessions(
                    session_id, peer_id, ip_address, listening_port,
                    status, login_at, last_seen, logout_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, session.sessionId());
            statement.setObject(2, session.peerId());
            statement.setString(3, session.ipAddress());
            statement.setInt(4, session.listeningPort());
            statement.setString(5, session.status().name());
            statement.setObject(6, session.loginAt());
            statement.setObject(7, session.lastSeen());
            statement.setObject(8, session.logoutAt());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<PeerSessionRecord> findBySessionId(UUID sessionId) throws SQLException {
        String sql = """
                SELECT session_id, peer_id, ip_address, listening_port,
                       status, login_at, last_seen, logout_at
                FROM peer_sessions
                WHERE session_id = ?
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, sessionId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    @Override
    public void closeActiveForPeer(UUID peerId, OffsetDateTime closedAt) throws SQLException {
        String sql = """
                UPDATE peer_sessions
                SET status = 'LOGGED_OUT', logout_at = ?
                WHERE peer_id = ? AND status = 'ONLINE'
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, closedAt);
            statement.setObject(2, peerId);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean updateLastSeen(UUID sessionId, OffsetDateTime lastSeen) throws SQLException {
        String sql = """
                UPDATE peer_sessions
                SET last_seen = ?
                WHERE session_id = ? AND status = 'ONLINE'
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, lastSeen);
            statement.setObject(2, sessionId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markLoggedOut(UUID sessionId, OffsetDateTime logoutAt) throws SQLException {
        String sql = """
                UPDATE peer_sessions
                SET status = 'LOGGED_OUT', logout_at = ?
                WHERE session_id = ? AND status = 'ONLINE'
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, logoutAt);
            statement.setObject(2, sessionId);
            return statement.executeUpdate() > 0;
        }
    }

    private static PeerSessionRecord map(ResultSet rs) throws SQLException {
        return new PeerSessionRecord(
                rs.getObject("session_id", UUID.class),
                rs.getObject("peer_id", UUID.class),
                rs.getString("ip_address"),
                rs.getInt("listening_port"),
                PeerStatus.valueOf(rs.getString("status")),
                toOffsetDateTime(rs.getTimestamp("login_at")),
                toOffsetDateTime(rs.getTimestamp("last_seen")),
                toOffsetDateTime(rs.getTimestamp("logout_at"))
        );
    }

    private static OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant().atOffset(ZoneOffset.UTC);
    }
}

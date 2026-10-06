package vn.edu.p2p.tracker.catalog;

import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class JdbcSessionPeerResolver implements SessionPeerResolver {

    private final DatabaseConnectionFactory connectionFactory;

    public JdbcSessionPeerResolver(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public UUID resolvePeerId(UUID sessionId) throws FileCatalogException, SQLException {
        String sql = """
                SELECT peer_id
                FROM peer_sessions
                WHERE session_id = ? AND status = 'ONLINE'
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, sessionId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new FileCatalogException("Session is not active: " + sessionId);
                }
                return rs.getObject("peer_id", UUID.class);
            }
        }
    }
}

package vn.edu.p2p.tracker.catalog;

import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public final class JdbcShareEventRepository implements ShareEventRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public JdbcShareEventRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void insert(UUID peerId, long fileId, String eventType) throws SQLException {
        String sql = """
                INSERT INTO share_events (peer_id, file_id, event_type)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, peerId);
            statement.setLong(2, fileId);
            statement.setString(3, eventType);
            statement.executeUpdate();
        }
    }
}

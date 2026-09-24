package vn.edu.p2p.tracker.repository.jdbc;

import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;
import vn.edu.p2p.tracker.repository.StatisticsRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JdbcStatisticsRepository implements StatisticsRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public JdbcStatisticsRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public long countRegisteredUsers() throws SQLException {
        return queryCount("SELECT COUNT(*) FROM users");
    }

    @Override
    public long countOnlinePeers() throws SQLException {
        return queryCount("""
                SELECT COUNT(DISTINCT peer_id)
                FROM peer_sessions
                WHERE status = 'ONLINE'
                """);
    }

    @Override
    public long countSharedFiles() throws SQLException {
        return queryCount("""
                SELECT COUNT(DISTINCT file_id)
                FROM peer_files
                WHERE is_sharing = TRUE
                """);
    }

    private long queryCount(String sql) throws SQLException {
        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return 0L;
            }
            return resultSet.getLong(1);
        }
    }
}

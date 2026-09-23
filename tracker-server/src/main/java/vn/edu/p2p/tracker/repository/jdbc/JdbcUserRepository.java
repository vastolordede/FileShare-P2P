package vn.edu.p2p.tracker.repository.jdbc;

import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;
import vn.edu.p2p.tracker.domain.AccountStatus;
import vn.edu.p2p.tracker.domain.UserRecord;
import vn.edu.p2p.tracker.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

public final class JdbcUserRepository implements UserRepository {
    private final DatabaseConnectionFactory connectionFactory;

    public JdbcUserRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<UserRecord> findByUsername(String username) throws SQLException {
        String sql = """
                SELECT user_id, username, password_hash, account_status, created_at
                FROM users
                WHERE LOWER(username) = LOWER(?)
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }
        }
    }

    @Override
    public UserRecord create(String username, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users(username, password_hash, account_status)
                VALUES (?, ?, 'ACTIVE')
                RETURNING user_id, username, password_hash, account_status, created_at
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("User insert returned no row");
                }
                return map(rs);
            }
        }
    }

    private static UserRecord map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        OffsetDateTime createdAt = created == null
                ? null
                : created.toInstant().atOffset(ZoneOffset.UTC);

        return new UserRecord(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                AccountStatus.valueOf(rs.getString("account_status")),
                createdAt
        );
    }
}

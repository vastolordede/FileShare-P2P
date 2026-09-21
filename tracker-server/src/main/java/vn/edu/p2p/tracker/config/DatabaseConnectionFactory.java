package vn.edu.p2p.tracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thin JDBC connection factory.
 *
 * Week 2 repositories will use this class.
 */
public final class DatabaseConnectionFactory {
    private final DatabaseConfig config;

    public DatabaseConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    public Connection open() throws SQLException {
        return DriverManager.getConnection(
                config.url(),
                config.username(),
                config.password()
        );
    }
}

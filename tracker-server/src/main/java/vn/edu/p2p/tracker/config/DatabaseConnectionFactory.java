package vn.edu.p2p.tracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    public void verify() throws SQLException {
        try (Connection connection = open();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT 1")) {
            if (!rs.next() || rs.getInt(1) != 1) {
                throw new SQLException("Database verification failed");
            }
        }
    }
}

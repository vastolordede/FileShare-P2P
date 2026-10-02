package vn.edu.p2p.tracker.catalog;

import vn.edu.p2p.common.dto.FileMetadata;
import vn.edu.p2p.common.dto.PieceInfo;
import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * JDBC-backed FileRepository, following the same connection/PreparedStatement
 * style as JdbcUserRepository / JdbcPeerRepository / JdbcFileSourceRepository.
 */
public final class JdbcFileRepository implements FileRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public JdbcFileRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Optional<Long> findFileIdByHash(String fileHash) throws SQLException {
        String sql = """
                SELECT file_id
                FROM files
                WHERE file_hash = ?
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fileHash);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getLong("file_id"));
            }
        }
    }

    @Override
    public long insert(FileMetadata metadata) throws SQLException {
        String insertFileSql = """
                INSERT INTO files (file_name, file_size, file_hash, piece_size, piece_count)
                VALUES (?, ?, ?, ?, ?)
                """;

        String insertPieceSql = """
                INSERT INTO file_pieces (file_id, piece_index, piece_length, piece_hash)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = connectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long fileId;

                try (PreparedStatement statement = connection.prepareStatement(
                        insertFileSql, Statement.RETURN_GENERATED_KEYS
                )) {
                    statement.setString(1, metadata.fileName());
                    statement.setLong(2, metadata.fileSize());
                    statement.setString(3, metadata.fileHash());
                    statement.setInt(4, metadata.pieceSize());
                    statement.setInt(5, metadata.pieceCount());
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Insert into files did not return a generated key");
                        }
                        fileId = keys.getLong(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(insertPieceSql)) {
                    for (PieceInfo piece : metadata.pieces()) {
                        statement.setLong(1, fileId);
                        statement.setInt(2, piece.pieceIndex());
                        statement.setInt(3, piece.pieceLength());
                        statement.setString(4, piece.pieceHash());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                connection.commit();
                return fileId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }
}

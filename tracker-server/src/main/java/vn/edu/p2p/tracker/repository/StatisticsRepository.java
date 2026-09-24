package vn.edu.p2p.tracker.repository;

import java.sql.SQLException;

public interface StatisticsRepository {
    long countRegisteredUsers() throws SQLException;

    long countOnlinePeers() throws SQLException;

    long countSharedFiles() throws SQLException;
}

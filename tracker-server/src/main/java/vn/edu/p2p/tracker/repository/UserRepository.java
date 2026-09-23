package vn.edu.p2p.tracker.repository;

import vn.edu.p2p.tracker.domain.UserRecord;

import java.sql.SQLException;
import java.util.Optional;

public interface UserRepository {
    Optional<UserRecord> findByUsername(String username) throws SQLException;

    UserRecord create(String username, String passwordHash) throws SQLException;
}

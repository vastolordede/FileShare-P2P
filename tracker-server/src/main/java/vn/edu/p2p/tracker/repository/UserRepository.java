package vn.edu.p2p.tracker.repository;

import vn.edu.p2p.tracker.domain.UserRecord;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Contract only for Week 1.
 * JDBC implementation is part of Week 2.
 */
public interface UserRepository {
    Optional<UserRecord> findByUsername(String username) throws SQLException;
}

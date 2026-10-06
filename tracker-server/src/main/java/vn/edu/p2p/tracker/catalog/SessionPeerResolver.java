package vn.edu.p2p.tracker.catalog;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Resolves an active sessionId to the peer_id that owns it.
 * Pulled out as an interface so FileCatalogService can be unit/integration
 * tested with a fake resolver instead of needing a live PostgreSQL session.
 */
public interface SessionPeerResolver {
    UUID resolvePeerId(UUID sessionId) throws FileCatalogException, SQLException;
}

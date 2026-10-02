package vn.edu.p2p.tracker.catalog;

import vn.edu.p2p.common.dto.FileMetadata;
import vn.edu.p2p.common.dto.ShareFileRequest;
import vn.edu.p2p.common.dto.ShareFileResponse;
import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic behind SHARE_FILE_REQUEST:
 *  - resolve the sessionId to an active peer
 *  - T13: detect duplicate files by SHA-256 hash
 *  - T11-T12: insert new file + pieces into the catalog when not a duplicate
 *  - T14-T15: record/refresh the sharing peer's ownership row
 *
 * NOTE: resolvePeerId(...) below runs its own direct query against
 * peer_sessions. If Đặng's PeerSessionRepository already exposes a method
 * to resolve peer_id from session_id, swap it in here instead - the rest
 * of the flow (dedupe -> insert -> ownership) stays unchanged.
 */
public final class FileCatalogService {

    private final DatabaseConnectionFactory connectionFactory;
    private final FileRepository fileRepository;
    private final PeerFileRepository peerFileRepository;

    public FileCatalogService(
            DatabaseConnectionFactory connectionFactory,
            FileRepository fileRepository,
            PeerFileRepository peerFileRepository
    ) {
        this.connectionFactory = connectionFactory;
        this.fileRepository = fileRepository;
        this.peerFileRepository = peerFileRepository;
    }

    public ShareFileResponse publish(ShareFileRequest request) throws FileCatalogException, SQLException {
        UUID sessionId = parseSessionId(request.sessionId());
        UUID peerId = resolvePeerId(sessionId);

        FileMetadata metadata = request.metadata();

        Optional<Long> existingFileId = fileRepository.findFileIdByHash(metadata.fileHash());

        long fileId;
        boolean isNewFile;

        if (existingFileId.isPresent()) {
            // T13: identical content already known to the Tracker - reuse
            // the existing file_id instead of inserting a duplicate row.
            fileId = existingFileId.get();
            isNewFile = false;
        } else {
            // T11-T12: publish new metadata into the File Catalog.
            fileId = fileRepository.insert(metadata);
            isNewFile = true;
        }

        // T14-T15: the sharing peer already holds the whole file locally,
        // so it is recorded as a COMPLETE, actively-sharing source.
        String availabilityStatus = "COMPLETE";
        peerFileRepository.upsertOwnership(peerId, fileId, availabilityStatus);

        return new ShareFileResponse(fileId, isNewFile, availabilityStatus);
    }

    private UUID parseSessionId(String sessionId) throws FileCatalogException {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new FileCatalogException("Invalid sessionId: " + sessionId);
        }
    }

    private UUID resolvePeerId(UUID sessionId) throws FileCatalogException, SQLException {
        String sql = """
                SELECT peer_id
                FROM peer_sessions
                WHERE session_id = ? AND status = 'ONLINE'
                """;

        try (Connection connection = connectionFactory.open();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, sessionId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new FileCatalogException("Session is not active: " + sessionId);
                }
                return rs.getObject("peer_id", UUID.class);
            }
        }
    }
}

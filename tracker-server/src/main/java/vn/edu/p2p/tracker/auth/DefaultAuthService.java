package vn.edu.p2p.tracker.auth;

import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LoginResponse;
import vn.edu.p2p.common.dto.LogoutRequest;
import vn.edu.p2p.tracker.domain.PeerRecord;
import vn.edu.p2p.tracker.domain.PeerSessionRecord;
import vn.edu.p2p.tracker.domain.PeerStatus;
import vn.edu.p2p.tracker.domain.UserRecord;
import vn.edu.p2p.tracker.repository.PeerRepository;
import vn.edu.p2p.tracker.repository.PeerSessionRepository;
import vn.edu.p2p.tracker.repository.UserRepository;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

public final class DefaultAuthService implements AuthService {
    private final UserRepository userRepository;
    private final PeerRepository peerRepository;
    private final PeerSessionRepository sessionRepository;
    private final PasswordService passwordService;
    private final int heartbeatIntervalSeconds;

    public DefaultAuthService(
            UserRepository userRepository,
            PeerRepository peerRepository,
            PeerSessionRepository sessionRepository,
            PasswordService passwordService,
            int heartbeatIntervalSeconds
    ) {
        this.userRepository = userRepository;
        this.peerRepository = peerRepository;
        this.sessionRepository = sessionRepository;
        this.passwordService = passwordService;
        this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
    }

    @Override
    public LoginResponse login(LoginRequest request, String remoteIp) throws AuthException {
        validateLoginRequest(request);

        try {
            Optional<UserRecord> optionalUser = userRepository.findByUsername(
                    request.username().trim()
            );

            if (optionalUser.isEmpty()) {
                throw invalidCredentials();
            }

            UserRecord user = optionalUser.get();
            if (!user.isActive()
                    || !passwordService.matches(request.password(), user.passwordHash())) {
                throw invalidCredentials();
            }

            UUID peerId = parseUuid(request.peerId(), "INVALID_PEER_ID");
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

            Optional<PeerRecord> existingPeer = peerRepository.findById(peerId);
            if (existingPeer.isPresent()) {
                if (existingPeer.get().userId() != user.userId()) {
                    throw new AuthException(
                            "PEER_OWNERSHIP_MISMATCH",
                            "Peer identity belongs to another account."
                    );
                }
                peerRepository.updateLastLogin(peerId, now);
            } else {
                peerRepository.create(new PeerRecord(
                        peerId,
                        user.userId(),
                        normalizeDeviceName(request.deviceName()),
                        now,
                        now
                ));
            }

            int replacedSessions = sessionRepository.closeActiveForPeer(peerId, now);
            if (replacedSessions > 0) {
                System.out.printf(
                        "Peer %s reconnect: replaced %d previous ONLINE session(s).%n",
                        peerId,
                        replacedSessions
                );
            }

            UUID sessionId = UUID.randomUUID();
            sessionRepository.create(new PeerSessionRecord(
                    sessionId,
                    peerId,
                    normalizeIp(remoteIp),
                    request.listeningPort(),
                    PeerStatus.ONLINE,
                    now,
                    now,
                    null
            ));

            return new LoginResponse(
                    peerId.toString(),
                    sessionId.toString(),
                    heartbeatIntervalSeconds
            );
        } catch (AuthException e) {
            throw e;
        } catch (SQLException e) {
            throw new AuthException(
                    "DATABASE_ERROR",
                    "Tracker cannot access the database.",
                    e
            );
        }
    }

    @Override
    public void logout(LogoutRequest request) throws AuthException {
        if (request == null || request.sessionId() == null || request.sessionId().isBlank()) {
            throw new AuthException("INVALID_SESSION", "Missing sessionId.");
        }

        UUID sessionId = parseUuid(request.sessionId(), "INVALID_SESSION");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        try {
            if (!sessionRepository.markLoggedOut(sessionId, now)) {
                throw new AuthException(
                        "INVALID_SESSION",
                        "Session is not online or does not exist."
                );
            }
        } catch (AuthException e) {
            throw e;
        } catch (SQLException e) {
            throw new AuthException(
                    "DATABASE_ERROR",
                    "Tracker cannot update the session.",
                    e
            );
        }
    }

    private static void validateLoginRequest(LoginRequest request) throws AuthException {
        if (request == null) {
            throw new AuthException("INVALID_REQUEST", "Missing login payload.");
        }
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw invalidCredentials();
        }
        if (request.listeningPort() < 1 || request.listeningPort() > 65535) {
            throw new AuthException("INVALID_PORT", "Invalid Peer listening port.");
        }
        if (request.peerId() == null || request.peerId().isBlank()) {
            throw new AuthException("INVALID_PEER_ID", "Missing Peer identity.");
        }
    }

    private static AuthException invalidCredentials() {
        return new AuthException(
                "INVALID_CREDENTIALS",
                "Invalid username or password."
        );
    }

    private static UUID parseUuid(String raw, String errorCode) throws AuthException {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            throw new AuthException(errorCode, "Invalid UUID value.", e);
        }
    }

    private static String normalizeDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return "Unknown device";
        }
        String trimmed = deviceName.trim();
        return trimmed.length() <= 100 ? trimmed : trimmed.substring(0, 100);
    }

    private static String normalizeIp(String remoteIp) {
        if (remoteIp == null || remoteIp.isBlank()) {
            return "unknown";
        }
        return remoteIp.length() <= 45 ? remoteIp : remoteIp.substring(0, 45);
    }
}

package vn.edu.p2p.tracker.auth;

import org.junit.jupiter.api.Test;
import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LoginResponse;
import vn.edu.p2p.common.dto.LogoutRequest;
import vn.edu.p2p.tracker.domain.AccountStatus;
import vn.edu.p2p.tracker.domain.PeerRecord;
import vn.edu.p2p.tracker.domain.PeerSessionRecord;
import vn.edu.p2p.tracker.domain.UserRecord;
import vn.edu.p2p.tracker.repository.PeerRepository;
import vn.edu.p2p.tracker.repository.PeerSessionRepository;
import vn.edu.p2p.tracker.repository.UserRepository;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultAuthServiceTest {

    @Test
    void shouldLoginAndLogout() throws Exception {
        FakeUserRepository users = new FakeUserRepository();
        FakePeerRepository peers = new FakePeerRepository();
        FakeSessionRepository sessions = new FakeSessionRepository();
        PasswordService passwords = new BCryptPasswordService(4);

        users.user = new UserRecord(
                1L,
                "dang",
                passwords.hash("secret"),
                AccountStatus.ACTIVE,
                OffsetDateTime.now()
        );

        DefaultAuthService service = new DefaultAuthService(
                users,
                peers,
                sessions,
                passwords,
                10
        );

        String peerId = UUID.randomUUID().toString();
        LoginResponse response = service.login(
                new LoginRequest("dang", "secret", 7001, peerId, "TEST-PC"),
                "127.0.0.1"
        );

        assertEquals(peerId, response.peerId());
        assertNotNull(response.sessionId());
        assertEquals(10, response.heartbeatIntervalSeconds());
        assertEquals(1, sessions.sessions.size());

        service.logout(new LogoutRequest(response.sessionId()));
        assertEquals("LOGGED_OUT", sessions.lastStatus);
    }

    @Test
    void shouldRejectWrongPassword() {
        FakeUserRepository users = new FakeUserRepository();
        FakePeerRepository peers = new FakePeerRepository();
        FakeSessionRepository sessions = new FakeSessionRepository();
        PasswordService passwords = new BCryptPasswordService(4);

        users.user = new UserRecord(
                1L,
                "dang",
                passwords.hash("secret"),
                AccountStatus.ACTIVE,
                OffsetDateTime.now()
        );

        DefaultAuthService service = new DefaultAuthService(
                users,
                peers,
                sessions,
                passwords,
                10
        );

        AuthException error = assertThrows(
                AuthException.class,
                () -> service.login(
                        new LoginRequest(
                                "dang",
                                "wrong",
                                7001,
                                UUID.randomUUID().toString(),
                                "TEST-PC"
                        ),
                        "127.0.0.1"
                )
        );

        assertEquals("INVALID_CREDENTIALS", error.errorCode());
    }

    private static final class FakeUserRepository implements UserRepository {
        UserRecord user;

        @Override
        public Optional<UserRecord> findByUsername(String username) {
            if (user != null && user.username().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
            return Optional.empty();
        }

        @Override
        public UserRecord create(String username, String passwordHash) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakePeerRepository implements PeerRepository {
        final Map<UUID, PeerRecord> peers = new HashMap<>();

        @Override
        public Optional<PeerRecord> findById(UUID peerId) {
            return Optional.ofNullable(peers.get(peerId));
        }

        @Override
        public void create(PeerRecord peer) {
            peers.put(peer.peerId(), peer);
        }

        @Override
        public void updateLastLogin(UUID peerId, OffsetDateTime lastLoginAt) {
            PeerRecord old = peers.get(peerId);
            peers.put(peerId, new PeerRecord(
                    old.peerId(),
                    old.userId(),
                    old.deviceName(),
                    old.createdAt(),
                    lastLoginAt
            ));
        }
    }

    private static final class FakeSessionRepository implements PeerSessionRepository {
        final Map<UUID, PeerSessionRecord> sessions = new HashMap<>();
        String lastStatus;

        @Override
        public void create(PeerSessionRecord session) {
            sessions.put(session.sessionId(), session);
        }

        @Override
        public Optional<PeerSessionRecord> findBySessionId(UUID sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }

        @Override
        public void closeActiveForPeer(UUID peerId, OffsetDateTime closedAt) {
            // no-op for this unit test
        }

        @Override
        public boolean updateLastSeen(UUID sessionId, OffsetDateTime lastSeen) {
            return sessions.containsKey(sessionId);
        }

        @Override
        public boolean markLoggedOut(UUID sessionId, OffsetDateTime logoutAt) {
            if (!sessions.containsKey(sessionId)) {
                return false;
            }
            lastStatus = "LOGGED_OUT";
            return true;
        }
    }
}

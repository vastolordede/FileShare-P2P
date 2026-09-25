package vn.edu.p2p.tracker.network;

import org.junit.jupiter.api.Test;
import vn.edu.p2p.common.dto.HeartbeatRequest;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;
import vn.edu.p2p.common.protocol.ProtocolCodec;
import vn.edu.p2p.common.protocol.ResponseStatus;
import vn.edu.p2p.tracker.auth.AuthException;
import vn.edu.p2p.tracker.auth.AuthService;
import vn.edu.p2p.tracker.domain.PeerSessionRecord;
import vn.edu.p2p.tracker.domain.PeerStatus;
import vn.edu.p2p.tracker.peer.PeerSessionService;
import vn.edu.p2p.tracker.repository.PeerSessionRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackerRequestDispatcherConcurrencyTest {
    @Test
    void shouldProcessConcurrentHeartbeatRequests() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID peerId = UUID.randomUUID();
        ConcurrentSessionRepository sessions = new ConcurrentSessionRepository();
        sessions.sessions.put(sessionId, new PeerSessionRecord(
                sessionId,
                peerId,
                "127.0.0.1",
                7001,
                PeerStatus.ONLINE,
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.now(ZoneOffset.UTC),
                null
        ));

        TrackerRequestDispatcher dispatcher = new TrackerRequestDispatcher(
                new NoopAuthService(),
                new PeerSessionService(sessions)
        );

        int requestCount = 64;
        ExecutorService executor = Executors.newFixedThreadPool(12);
        try {
            var futures = IntStream.range(0, requestCount)
                    .mapToObj(i -> executor.submit(() -> dispatcher.dispatch(
                            MessageEnvelope.request(
                                    MessageType.HEARTBEAT,
                                    "hb-" + i,
                                    ProtocolCodec.toPayload(
                                            new HeartbeatRequest(sessionId.toString())
                                    )
                            ),
                            "127.0.0.1"
                    )))
                    .toList();

            for (Future<MessageEnvelope> future : futures) {
                assertEquals(ResponseStatus.SUCCESS, future.get().status());
            }
            assertEquals(requestCount, sessions.heartbeatUpdates.get());
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class NoopAuthService implements AuthService {
        @Override
        public vn.edu.p2p.common.dto.LoginResponse login(
                vn.edu.p2p.common.dto.LoginRequest request,
                String remoteIp
        ) throws AuthException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void logout(vn.edu.p2p.common.dto.LogoutRequest request)
                throws AuthException {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ConcurrentSessionRepository
            implements PeerSessionRepository {
        final ConcurrentHashMap<UUID, PeerSessionRecord> sessions =
                new ConcurrentHashMap<>();
        final AtomicInteger heartbeatUpdates = new AtomicInteger();

        @Override
        public void create(PeerSessionRecord session) {
            sessions.put(session.sessionId(), session);
        }

        @Override
        public Optional<PeerSessionRecord> findBySessionId(UUID sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }

        @Override
        public int closeActiveForPeer(UUID peerId, OffsetDateTime closedAt) {
            return 0;
        }

        @Override
        public boolean updateLastSeen(UUID sessionId, OffsetDateTime lastSeen) {
            final boolean[] updated = {false};
            sessions.computeIfPresent(sessionId, (id, current) -> {
                if (current.status() != PeerStatus.ONLINE) {
                    return current;
                }
                heartbeatUpdates.incrementAndGet();
                updated[0] = true;
                return new PeerSessionRecord(
                        current.sessionId(),
                        current.peerId(),
                        current.ipAddress(),
                        current.listeningPort(),
                        current.status(),
                        current.loginAt(),
                        lastSeen,
                        current.logoutAt()
                );
            });
            return updated[0];
        }

        @Override
        public boolean markLoggedOut(UUID sessionId, OffsetDateTime logoutAt) {
            return false;
        }

        @Override
        public int expireStaleSessions(OffsetDateTime staleBefore) {
            return 0;
        }
    }
}

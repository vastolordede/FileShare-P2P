# Week 6 — Tracker core regression and concurrency test plan

Covers D43-D46.

## Automated

- Login/Auth/Session: `DefaultAuthServiceTest`
- Heartbeat + session validation: `PeerSessionServiceTest`
- Online/Offline timeout: `HeartbeatMonitorTest`
- Concurrent request dispatch: `TrackerRequestDispatcherConcurrencyTest`
- Concurrent reconnects: `DefaultAuthServiceTest.concurrentReconnectsShouldLeaveSingleOnlineSession`

## Manual regression

1. Start PostgreSQL and Tracker with TLS enabled.
2. Login a Peer, observe ONLINE, heartbeat, then logout.
3. Login the same stable Peer id from two client instances close together; only one ONLINE `peer_sessions` row must remain.
4. Let one client stop heartbeating; it must become EXPIRED.
5. Run `./gradlew clean test` repeatedly to catch order/race-sensitive failures.

The JDBC session replacement is serialized by `SELECT ... FOR UPDATE` on the Peer row and performs close-old + insert-new in one transaction.

# Week 5 — Tracker hardening test plan

Covers D38-D41.

1. `./gradlew clean test` and `./gradlew build` succeed.
2. Start Tracker with TLS enabled and confirm the configured worker count, queue capacity and socket timeout are printed.
3. Login from several Peer processes concurrently; Tracker remains responsive.
4. Send malformed/invalid request payloads and verify a normalized `status=ERROR`, `errorCode`, and `message` response.
5. Open an idle TCP/TLS client and verify Tracker closes it after `TRACKER_SOCKET_READ_TIMEOUT_MILLIS`.
6. Verify valid Login, Heartbeat and Logout still work after hardening.

The bounded queue prevents unlimited pending client tasks; rejected connections are closed instead of accumulating indefinitely.

# Week 7 — D48 Full Tracker Integration Test

## Goal

Close the independent Tracker/Core work by verifying the complete control flow through
the real TCP server and protocol stack:

1. LOGIN_REQUEST -> LOGIN_RESPONSE
2. HEARTBEAT -> HEARTBEAT_ACK
3. FILE_SOURCES_REQUEST -> FILE_SOURCES_RESPONSE
4. same Peer reconnects -> previous ONLINE session is replaced
5. heartbeat from the old session is rejected
6. heartbeat from the new session succeeds
7. LOGOUT_REQUEST -> LOGOUT_RESPONSE
8. heartbeat after logout is rejected

The automated integration test uses the real:
- TrackerServer
- ClientHandler
- ProtocolCodec TCP framing
- TrackerRequestDispatcher
- DefaultAuthService
- PeerSessionService
- FileSourceService

PostgreSQL is replaced by thread-safe in-memory repositories so the Gradle test is
portable and does not require developer credentials.

## Why TLS/PostgreSQL are not embedded in the JUnit test

`.env`, keystores, truststores and database credentials are intentionally local-only
and ignored by Git. TLS + real PostgreSQL therefore remain a final manual smoke check,
using the already configured local environment.

## Automated verification

From the project root:

```bat
.\gradlew.bat clean test
.\gradlew.bat build
```

The following test must pass:

```text
vn.edu.p2p.tracker.integration.TrackerEndToEndIntegrationTest
```

## Final local smoke test

Terminal 1:

```bat
.\gradlew.bat :tracker-server:run
```

Terminal 2:

```bat
.\gradlew.bat :peer-client:run
```

Verify:

```text
Login -> ONLINE -> heartbeat updates -> Logout -> LOGGED OUT
```

With Tracker TLS enabled locally, this smoke test also reconfirms that the integrated
core still works over TLS and the real PostgreSQL database.

## Sheet mapping

- D48 — Full Tracker integration test

Only mark D48 `Đã Hoàn Thành` after both:
- automated Gradle tests/build pass;
- final local Tracker + Peer smoke test passes.

No database schema change is required.

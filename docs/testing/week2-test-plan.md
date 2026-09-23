# Week 2 test plan — Account & Tracker Control

## Scope

Week 2 must prove the following end-to-end flow:

1. Tracker opens a real TCP `ServerSocket` on port 9000.
2. Tracker connects to PostgreSQL `fileshare_p2p`.
3. Passwords are stored as BCrypt hashes, never plaintext.
4. JavaFX Peer sends `LOGIN_REQUEST` over TCP.
5. Tracker verifies credentials and creates/updates `peers` + `peer_sessions`.
6. GUI receives `LOGIN_RESPONSE` and shows login success.
7. `LOGOUT_REQUEST` changes the active session to `LOGGED_OUT`.

Heartbeat persistence remains Week 3 work.

## Local configuration

Create or edit:

`tracker-server/src/main/resources/application.properties`

```properties
db.url=jdbc:postgresql://localhost:5432/fileshare_p2p
db.username=postgres
db.password=YOUR_POSTGRES_PASSWORD
tracker.port=9000
heartbeat.interval.seconds=10
heartbeat.timeout.seconds=30
```

`application.properties` is ignored by Git.

## Build / unit test

```bat
.\gradlew.bat clean test
.\gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`.

## Create a test account

```bat
.\gradlew.bat :tracker-server:createUser --args="dang Test123!"
```

Expected: `Created ACTIVE user: dang`

## Start Tracker

Terminal 1:

```bat
.\gradlew.bat :tracker-server:run
```

Expected output contains:

- `DB check : OK`
- `Tracker TCP server listening on 0.0.0.0:9000`

Leave this terminal running.

## Start Peer GUI

Terminal 2:

```bat
.\gradlew.bat :peer-client:run
```

Login with the test account and listening port `7001`.

Expected:

- GUI shows `Đăng nhập thành công`.
- `peers` contains the local Peer UUID.
- `peer_sessions` contains one `ONLINE` row.

Press `LOGOUT`.

Expected:

- GUI shows `Đã đăng xuất khỏi Tracker`.
- the same `peer_sessions` row becomes `LOGGED_OUT` and gets `logout_at`.

## Verification queries

```sql
SELECT user_id, username, account_status, created_at
FROM users
ORDER BY user_id;

SELECT peer_id, user_id, device_name, last_login_at
FROM peers
ORDER BY created_at DESC;

SELECT session_id, peer_id, ip_address, listening_port,
       status, login_at, last_seen, logout_at
FROM peer_sessions
ORDER BY login_at DESC;
```

## Negative tests

- Wrong password -> `INVALID_CREDENTIALS`.
- Tracker stopped -> GUI reports network error without freezing.
- Invalid listening port -> GUI rejects the form before network I/O.
- Login again with the same local Peer -> previous ONLINE session is closed before a new one is created.

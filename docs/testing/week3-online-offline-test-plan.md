# Week 3 — Online / Offline test plan

Default policy:

- heartbeat interval: 10 seconds
- timeout: 30 seconds
- monitor scan: every 10 seconds

## Manual

1. Start Tracker.
2. Login one Peer.
3. Confirm newest row is `ONLINE`.
4. Stop/kill the Peer process without logout.
5. Wait more than 30 seconds plus one monitor scan.
6. Query:

```sql
SELECT session_id, peer_id, status, last_seen, logout_at
FROM peer_sessions
ORDER BY login_at DESC;
```

Expected: the abandoned session becomes `EXPIRED`.

## Explicit logout

Login again and press Logout. The current session must become `LOGGED_OUT`
immediately; it must not remain `ONLINE`.

## Automated

```bat
.\gradlew.bat clean test
```

`HeartbeatMonitorTest` verifies stale ONLINE sessions expire while a fresh
session remains ONLINE.

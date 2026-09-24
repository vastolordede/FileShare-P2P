# Week 4 — Reconnect & session lifecycle test plan

## Scope

This patch hardens the existing Week 2/3 account/session flow without changing
the database schema.

Covered master-checklist items:

- D28 — Peer reconnect handling
- D29 — do not keep duplicate ONLINE sessions for the same Peer
- graceful JavaFX shutdown sends LOGOUT best-effort
- regression checks for Login / Logout / Heartbeat session state

## Automated checks

Run:

```bat
.\gradlew.bat clean test
.\gradlew.bat build
```

`DefaultAuthServiceTest.shouldReplacePreviousOnlineSessionWhenSamePeerReconnects`
must pass.

## PostgreSQL smoke test

1. Start Tracker.
2. Login from Peer A.
3. Query:

```sql
SELECT session_id, peer_id, status, login_at, last_seen, logout_at
FROM peer_sessions
ORDER BY login_at DESC;
```

4. Login again from the same Peer identity.
5. Confirm only one row for that `peer_id` is `ONLINE`.
6. Close the JavaFX application normally.
7. Confirm the newest session becomes `LOGGED_OUT`.

A process killed from Task Manager cannot perform graceful logout; that case is
still handled by the Week 3 heartbeat timeout and becomes `EXPIRED`.

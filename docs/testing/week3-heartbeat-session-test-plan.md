# Week 3 — Heartbeat & Session test plan

## Automated

```bat
.\gradlew.bat clean test
.\gradlew.bat build
```

The Week 3 tests verify:

- a valid ONLINE session accepts HEARTBEAT,
- `last_seen` is refreshed,
- malformed session IDs are rejected,
- LOGGED_OUT sessions cannot heartbeat.

## Manual DB check

After a Peer logs in, note its `session_id`:

```sql
SELECT session_id, peer_id, status, last_seen
FROM peer_sessions
ORDER BY login_at DESC;
```

Send/observe a heartbeat and run the query again. `last_seen` must advance while
`status` remains `ONLINE`.

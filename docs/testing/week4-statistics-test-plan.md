# Week 4 — Tracker statistics test plan

Covered checklist items:

- D30 — registered user count
- D31 — online Peer count
- D32 — shared file count
- D33 — Tracker console monitor

No database migration is required. The monitor queries the existing core schema.

## Automated checks

```bat
.\gradlew.bat clean test
.\gradlew.bat build
```

## Manual PostgreSQL check

Start Tracker and compare the console values against:

```sql
SELECT COUNT(*) AS users FROM users;

SELECT COUNT(DISTINCT peer_id) AS online_peers
FROM peer_sessions
WHERE status = 'ONLINE';

SELECT COUNT(DISTINCT file_id) AS shared_files
FROM peer_files
WHERE is_sharing = TRUE;
```

Tracker prints a snapshot immediately on startup and then every
`statistics.interval.seconds` seconds.

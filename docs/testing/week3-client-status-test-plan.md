# Week 3 — Peer heartbeat & connection status GUI

## Expected GUI states

After successful login:

```text
Tracker: ONLINE
```

After each successful heartbeat the label shows the latest Tracker heartbeat
time.

If the network/Tracker disappears:

```text
Tracker: CONNECTION LOST
```

The heartbeat scheduler keeps retrying at the configured interval.

If the Tracker reports that the session is expired or no longer online, the
local session is cleared and the Login form is enabled again.

## Manual test

1. Start Tracker and Peer.
2. Login.
3. Confirm the GUI becomes ONLINE.
4. Query `peer_sessions.last_seen` several times; it should advance.
5. Stop Tracker for a short period. GUI should report CONNECTION LOST.
6. Restart Tracker before the session timeout; heartbeat should recover.
7. For expiration test, keep Tracker unavailable beyond timeout, restart it,
   and wait for the next heartbeat. Peer should require login again.
8. Login once more and press Logout. DB status must become `LOGGED_OUT`.

## Build

```bat
.\gradlew.bat clean test
.\gradlew.bat build
.\gradlew.bat :peer-client:run
```

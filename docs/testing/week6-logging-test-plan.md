# Week 6 — Tracker server logging test plan

Covers D47.

1. Set `TRACKER_LOG_LEVEL=INFO` and start Tracker.
2. Verify startup configuration is logged without passwords or TLS secret values.
3. Login/reconnect a Peer and verify reconnect/session events are logged.
4. Stop heartbeat and verify expiry is logged.
5. Trigger an invalid request and verify the client receives a normalized ERROR while unexpected server exceptions are logged with a stack trace.
6. Set `TRACKER_LOG_LEVEL=FINE` to enable connection-close diagnostics.

The project uses the JDK `java.util.logging` API; no extra logging dependency is required.

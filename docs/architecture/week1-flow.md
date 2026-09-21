# Week 1 — Account & Tracker Control Flow

## Login

```text
Peer Client
    |
    | TCP/TLS (TLS enabled before real credentials are used)
    v
Tracker
    |
    v
AuthService
    |
    v
UserRepository
    |
    v
PostgreSQL

Valid:
  create peerId
  create sessionId
  store peer session
  -> LOGIN_RESPONSE SUCCESS

Invalid:
  -> LOGIN_RESPONSE ERROR / INVALID_CREDENTIALS
```

Tracker should obtain the remote IP from the accepted socket rather than
trusting an IP string supplied by the Peer.

## Heartbeat

```text
Peer -- HEARTBEAT(sessionId) --> Tracker
Tracker updates last_seen
Tracker -- HEARTBEAT_ACK --> Peer
```

Baseline:

- heartbeat interval: 10 seconds
- offline timeout: 30 seconds

A Peer is considered timed out only after it exceeds the timeout.

## Logout

```text
Peer -- LOGOUT_REQUEST(sessionId) --> Tracker
Tracker marks session OFFLINE and sets logout_at
Tracker -- LOGOUT_RESPONSE --> Peer
```

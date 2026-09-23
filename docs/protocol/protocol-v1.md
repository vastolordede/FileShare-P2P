# P2P Control Protocol v1

## Framing

TCP is a byte stream. A control message is encoded as:

```text
+----------------------+-------------------------+
| JSON length (4 byte) | JSON UTF-8 (N bytes)   |
+----------------------+-------------------------+
```

`ProtocolCodec` implements this framing. Maximum control JSON size: **1 MiB**.

## Common envelope

```json
{
  "version": 1,
  "type": "LOGIN_REQUEST",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {}
}
```

A response contains `status=SUCCESS` or `status=ERROR`. Error responses may also
contain `errorCode` and `message`.

## LOGIN_REQUEST

```json
{
  "version": 1,
  "type": "LOGIN_REQUEST",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "username": "dang",
    "password": "Test123!",
    "listeningPort": 7001,
    "peerId": "d50bb84a-2924-4f42-a533-57c6bc72ca63",
    "deviceName": "DESKTOP-ABC"
  }
}
```

`peerId` is a stable UUID stored locally by each Peer. The Tracker uses it to
associate repeated logins with the same machine/client record.

### Week 2 security note

Week 2 implements the required TCP login milestone. The password is still sent
inside the TCP control message, so **use test credentials only**. Before the
final security milestone, the same protocol must run over TLS (`SSLSocket` /
`SSLServerSocket`) so credentials and control traffic are encrypted in transit.
Passwords stored in PostgreSQL are already BCrypt hashes.

## LOGIN_RESPONSE — success

```json
{
  "version": 1,
  "type": "LOGIN_RESPONSE",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "payload": {
    "peerId": "d50bb84a-2924-4f42-a533-57c6bc72ca63",
    "sessionId": "caf975e7-db6f-4a10-a08d-361be518fe78",
    "heartbeatIntervalSeconds": 10
  }
}
```

## LOGIN_RESPONSE — error

```json
{
  "version": 1,
  "type": "LOGIN_RESPONSE",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ERROR",
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid username or password."
}
```

Do not reveal whether the username or password was the failing field.

## LOGOUT_REQUEST

```json
{
  "version": 1,
  "type": "LOGOUT_REQUEST",
  "requestId": "04f83379-1ce2-4ef3-a125-49d64e797608",
  "payload": {
    "sessionId": "caf975e7-db6f-4a10-a08d-361be518fe78"
  }
}
```

## LOGOUT_RESPONSE

```json
{
  "version": 1,
  "type": "LOGOUT_RESPONSE",
  "requestId": "04f83379-1ce2-4ef3-a125-49d64e797608",
  "status": "SUCCESS"
}
```

## HEARTBEAT

Peer sends a heartbeat after login using the `sessionId` returned by
`LOGIN_RESPONSE`.

```json
{
  "version": 1,
  "type": "HEARTBEAT",
  "requestId": "52abe927-ef30-4057-a821-d5b92aec17ee",
  "payload": {
    "sessionId": "caf975e7-db6f-4a10-a08d-361be518fe78"
  }
}
```

The Tracker accepts heartbeat only for a session whose status is `ONLINE`,
updates `peer_sessions.last_seen`, and replies:

```json
{
  "version": 1,
  "type": "HEARTBEAT_ACK",
  "requestId": "52abe927-ef30-4057-a821-d5b92aec17ee",
  "status": "SUCCESS",
  "payload": {
    "serverTimeEpochMillis": 1790208000000
  }
}
```

An invalid, expired or logged-out session receives an error response instead.
The Online/Offline timeout monitor is implemented as the next Week 3 cluster.

## Extension rule for the team

Other features must extend `MessageType` and keep this envelope/framing. Do not
create a second incompatible control protocol.

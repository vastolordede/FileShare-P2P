# P2P Control Protocol v1

## Framing

TCP is a byte stream. A message is encoded as:

```text
+----------------------+-------------------------+
| JSON length (4 byte) | JSON UTF-8 (N bytes)   |
+----------------------+-------------------------+
```

`ProtocolCodec` implements this framing.

Maximum control JSON size in Week 1: **1 MiB**.

## Common envelope

```json
{
  "version": 1,
  "type": "LOGIN_REQUEST",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {}
}
```

Response can contain:

```json
{
  "status": "SUCCESS"
}
```

or:

```json
{
  "status": "ERROR",
  "errorCode": "INVALID_CREDENTIALS",
  "message": "Invalid username or password"
}
```

## LOGIN_REQUEST

```json
{
  "version": 1,
  "type": "LOGIN_REQUEST",
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "payload": {
    "username": "dang",
    "password": "123456",
    "listeningPort": 7001
  }
}
```

The password is never stored plaintext. Before real credentials are sent,
the TCP channel must be protected by TLS.

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
  "message": "Invalid username or password"
}
```

Do not distinguish “username exists” from “password is wrong” in the
external response.

## HEARTBEAT

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

## HEARTBEAT_ACK

```json
{
  "version": 1,
  "type": "HEARTBEAT_ACK",
  "requestId": "52abe927-ef30-4057-a821-d5b92aec17ee",
  "status": "SUCCESS",
  "payload": {
    "serverTimeEpochMillis": 1789981200000
  }
}
```

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

## Extension rule for the team

Other features must extend `MessageType` and keep the same envelope/framing.
Do not create a second incompatible protocol.

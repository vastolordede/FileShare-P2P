# Database notes — Week 1

## users

Stores persistent account information.

- `user_id`: internal primary key
- `username`: unique login name
- `password_hash`: BCrypt hash only
- `is_active`: account state
- `created_at`

## peer_sessions

Stores login/network sessions.

- `session_id`: authentication/session identifier
- `peer_id`: Peer identifier returned to client
- `user_id`: account owner
- `ip_address`: Tracker should derive this from accepted socket
- `listening_port`: Peer upload-listener port announced by client
- `status`: ONLINE/OFFLINE
- `login_at`
- `last_seen`: updated by HEARTBEAT
- `logout_at`

Week 1 intentionally does not add `files`, `peer_files` or
`transfer_history`; those belong to the corresponding vertical features.

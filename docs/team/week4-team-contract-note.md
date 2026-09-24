# Shared contract after Week 3

This note is for Share/File Discovery and Transfer Engine contributors.

- `database/schema.sql` is the current core DB contract. Do not create a second
  account/session schema in another module.
- Tracker owns `users`, `peers`, and `peer_sessions` lifecycle.
- After login a Peer has `peerId` + `sessionId`; Tracker heartbeat is already
  handled by the account/client layer.
- Other Tracker commands that require authentication should reuse the existing
  session validation service instead of inventing another login/token format.
- `peer_files` stores only PARTIAL/COMPLETE ownership and share state; the live
  BitSet/bitfield remains a Peer runtime concern.
- Transfer/Share tables already exist as persistence contracts, but their
  application logic still belongs to the assigned owner.
- TLS for Peer ↔ Tracker is introduced in Week 4. The same certificate/trust
  pattern can be reused later for Peer ↔ Peer transport.

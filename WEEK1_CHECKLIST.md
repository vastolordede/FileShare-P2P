# Week 1 Checklist — Người 1

- [x] Multi-module project skeleton
- [x] `common`
- [x] `tracker-server`
- [x] `peer-client`
- [x] PostgreSQL Week 1 schema
- [x] `users`
- [x] `peer_sessions`
- [x] Login flow
- [x] Logout flow
- [x] Heartbeat flow
- [x] Protocol envelope v1
- [x] LOGIN request/response DTOs
- [x] LOGOUT request DTO
- [x] HEARTBEAT request/response DTOs
- [x] 4-byte length + JSON framing
- [x] JavaFX Login skeleton
- [x] Heartbeat 10s / timeout 30s rule
- [x] Repository contracts for Week 2
- [x] Unit tests for codec / heartbeat / login validation

## Not implemented yet (planned Week 2)

- [ ] Real Tracker TCP accept loop
- [ ] TLS socket initialization
- [ ] JDBC repository implementations
- [ ] BCrypt password verification
- [ ] Real LOGIN/LOGOUT processing
- [ ] Heartbeat scheduler connected to DB/session manager
- [ ] JavaFX login calling the real Tracker

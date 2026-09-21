# FileShare-P2P — Week 1 Starter

Starter project cho đồ án **P2P File Sharing** theo scope đã chốt.

## Phạm vi gói Week 1

Phần của **Người 1 — Account & Tracker Control**:

- Khởi tạo Gradle multi-module.
- Chốt cấu trúc `common`, `tracker-server`, `peer-client`.
- PostgreSQL schema ban đầu cho `users`, `peer_sessions`.
- Chốt protocol v1 cho:
  - `LOGIN_REQUEST / LOGIN_RESPONSE`
  - `LOGOUT_REQUEST / LOGOUT_RESPONSE`
  - `HEARTBEAT / HEARTBEAT_ACK`
- Chốt framing: **4-byte length + JSON UTF-8**.
- DTO/model dùng chung.
- Skeleton Tracker configuration/repository.
- JavaFX Login UI skeleton.
- Login flow / Logout flow / Heartbeat flow.
- Unit test cho protocol codec.

> Week 1 **chưa triển khai login thật qua TCP và PostgreSQL**. Phần đó là Week 2.

## Stack

- Java 21
- Gradle
- JavaFX 21
- PostgreSQL + JDBC
- Jackson
- BCrypt (đã khai báo dependency để dùng từ Week 2)
- JUnit 5

## Cấu trúc

```text
FileShare-P2P/
├── common/
├── tracker-server/
├── peer-client/
├── database/
└── docs/
```

## Chuẩn bị PostgreSQL

Tạo database:

```sql
CREATE DATABASE fileshare_p2p;
```

Sau đó chạy:

```text
database/schema.sql
```

Copy:

```text
tracker-server/src/main/resources/application.example.properties
```

thành:

```text
tracker-server/src/main/resources/application.properties
```

rồi sửa username/password PostgreSQL trên máy cá nhân.

`application.properties` đã nằm trong `.gitignore`, không commit mật khẩu thật lên Git.

## Chạy trong IntelliJ IDEA

1. Mở thư mục gốc `FileShare-P2P`.
2. Chọn **Trust Project** nếu IntelliJ hỏi.
3. Chờ Gradle Sync tải dependencies.
4. Chọn JDK 21 cho Gradle JVM.
5. Chạy:
   - `vn.edu.p2p.peer.PeerApplication` để xem Login UI.
   - `vn.edu.p2p.tracker.TrackerApplication` để kiểm tra Tracker Week 1 config.

Hoặc dùng Gradle:

```bash
gradle :peer-client:run
gradle :tracker-server:run
gradle test
```

## Quy ước chung cần giữ

- `Tracker` chỉ truyền control/metadata; **không truyền nội dung file**.
- Peer ↔ Tracker và Peer ↔ Peer đều dùng TCP; TLS sẽ được tích hợp trước khi truyền credential/data thật.
- IP của Peer về sau nên lấy từ socket phía Tracker; client chỉ công bố `listeningPort`.
- Password **không lưu plaintext**. Week 2 dùng BCrypt.
- SHA-256 dành cho file integrity, không dùng thay password hashing.
- Tất cả message điều khiển dùng `MessageEnvelope` và `ProtocolCodec` trong module `common`.

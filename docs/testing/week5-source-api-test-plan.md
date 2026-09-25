# Week 5 — File source availability API test plan

Covers D42 and dependency T26.

1. Login Peer A and Peer B.
2. Ensure `peer_files` contains a sharing row for Peer A and the requested file.
3. From Peer B/session B send `FILE_SOURCES_REQUEST` with the file id.
4. Verify `FILE_SOURCES_RESPONSE` contains Peer A endpoint and availability status.
5. Verify Peer B is excluded from its own source list.
6. Mark Peer A offline or stop sharing; verify it disappears from the response.
7. Send an invalid/offline session id; verify normalized ERROR response.

No file bytes are transferred by this API. It only supplies metadata/endpoints to the downloader.

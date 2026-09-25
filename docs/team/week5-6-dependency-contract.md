# Week 5–6 dependency contract

This note prevents duplicate implementations while the team works in parallel.

## Thảo — Share / File Discovery

The Tracker-side source availability dependency required by Week 5 is implemented through:

- `FILE_SOURCES_REQUEST / FILE_SOURCES_RESPONSE`
- `FileSourcesRequest`, `FileSourcesResponse`, `FileSourceInfo`
- existing database view `v_file_sources`

The API returns only sources already present in `v_file_sources` (online + sharing) and excludes the requesting Peer. Thảo should reuse this contract rather than introduce a second source-discovery protocol/table. File metadata continues to use `files`, `file_pieces`, and `peer_files`.

## Sang — Transfer Engine

The source API above is the Tracker hand-off point for the downloader. File bytes still travel directly Peer-to-Peer; Tracker must not relay file content.

Peer-to-Tracker transport already supports TLS. Peer-to-Peer TLS should reuse the common TLS utilities/config pattern rather than bypassing TLS or placing credentials/certificates in Git.

## Local-only files

Do not commit `.env`, `certs/`, keystores/truststores, or `work/`.

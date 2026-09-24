# Week 4 — TLS test plan

Covered checklist items:

- D34 — local keystore/self-signed certificate workflow
- D35 — Peer ↔ Tracker TLS transport
- D36 — certificate/truststore configuration
- D37 — TLS handshake + encrypted session transport

## Build

```bat
.\gradlew.bat clean test
.\gradlew.bat build
```

## Manual matrix

1. Tracker TLS=false, Peer TLS=false → login succeeds (development fallback).
2. Tracker TLS=true, Peer TLS=true with correct truststore → login succeeds.
3. Tracker TLS=true, Peer TLS=true with wrong/untrusted truststore → login fails.
4. Tracker TLS=true, Peer TLS=false → login fails.
5. Confirm heartbeat and logout still work over TLS.

Use `docs/security/tls-setup.md` to generate local stores.

No certificate, password, `.p12`, `.jks`, `.pem`, or private key should be
committed to Git.

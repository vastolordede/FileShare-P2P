# TLS setup — Peer ↔ Tracker

Week 4 adds an optional TLS transport for the existing framed JSON protocol.
The application protocol does not change: TLS only wraps the TCP socket.

Certificates/keystores are local secrets and remain ignored by Git.

## 1. Create local certificate directory

From the project root:

```bat
mkdir certs
```

## 2. Create a self-signed Tracker certificate

For localhost development:

```bat
keytool -genkeypair ^
  -alias tracker ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 3650 ^
  -storetype PKCS12 ^
  -keystore certs\tracker-server.p12 ^
  -storepass changeit ^
  -keypass changeit ^
  -dname "CN=FileShare-P2P Tracker, OU=Student, O=FileShare-P2P, C=VN"
```

Export the public certificate:

```bat
keytool -exportcert ^
  -alias tracker ^
  -keystore certs\tracker-server.p12 ^
  -storetype PKCS12 ^
  -storepass changeit ^
  -rfc ^
  -file certs\tracker-cert.pem
```

Create the Peer truststore:

```bat
keytool -importcert ^
  -alias tracker ^
  -file certs\tracker-cert.pem ^
  -keystore certs\peer-truststore.p12 ^
  -storetype PKCS12 ^
  -storepass changeit ^
  -noprompt
```

## 3. Recommended local configuration

Week 4.2 introduced project-level `.env` support. Keep using the same `.env`
instead of putting TLS passwords into tracked example files.

Add or update:

```env
TRACKER_TLS_ENABLED=true
TRACKER_TLS_KEYSTORE=certs/tracker-server.p12
TRACKER_TLS_KEYSTORE_PASSWORD=changeit
TRACKER_TLS_KEYSTORE_TYPE=PKCS12

TRACKER_TRUSTSTORE=certs/peer-truststore.p12
TRACKER_TRUSTSTORE_PASSWORD=changeit
TRACKER_TRUSTSTORE_TYPE=PKCS12
```

`TRACKER_TLS_ENABLED` is shared by Tracker and Peer so both sides switch
transport together during local development.

`application.properties` remains supported as a fallback, but `.env` is the
recommended local source for secrets.

## 5. Test

Start Tracker and confirm:

```text
Transport     : TLS
Tracker TLS server listening ...
```

Then run the Peer and login normally.

If Tracker has TLS enabled while Peer does not (or the reverse), the connection
must fail. Both sides must use the same transport mode.

## Notes

- TLS negotiates keys during its handshake and then encrypts application data
  with symmetric session keys. Do not add a second home-made encryption layer
  around the same Tracker control messages.
- The same `common.security.TlsContextFactory` can be reused later by the
  Peer ↔ Peer upload/download socket owned by the Transfer Engine.
- For LAN demonstration, keep the private Tracker keystore on the Tracker
  machine and distribute only the truststore/public certificate to Peers.

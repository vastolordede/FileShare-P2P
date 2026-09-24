# Relative local paths

Paths from `.env`, such as TLS keystore/truststore locations, are resolved
relative to the directory containing `.env`.

Example from project-root `.env`:

```env
TRACKER_TLS_KEYSTORE=certs/tracker-server.p12
TRACKER_TRUSTSTORE=certs/peer-truststore.p12
```

This remains valid when Gradle executes `:tracker-server:run` or
`:peer-client:run` with a subproject working directory.

Absolute paths are also supported.

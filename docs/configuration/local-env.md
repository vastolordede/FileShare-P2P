# Local `.env` configuration

Tracker configuration priority:

1. Windows/Linux environment variables
2. project `.env`
3. `tracker-server/src/main/resources/application.properties`
4. built-in defaults

Recommended local setup:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=fileshare_p2p
DB_USER=postgres
DB_PASSWORD=<your-local-password>

TRACKER_PORT=9000
HEARTBEAT_INTERVAL_SECONDS=10
HEARTBEAT_TIMEOUT_SECONDS=30
STATISTICS_INTERVAL_SECONDS=30
```

The loader checks `.env` in the current working directory, its parent,
and `tracker-server/.env`. You can also point to another file with
the OS environment variable `P2P_ENV_FILE`.

Do not commit `.env`. Commit `.env.example` only.

Existing `application.properties` remains supported for compatibility.

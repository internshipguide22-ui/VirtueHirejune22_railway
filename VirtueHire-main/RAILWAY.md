# Railway deployment

Create one Railway project containing these services:

1. A MySQL database.
2. A backend service using root directory `/VirtueHire-main/Backend`.
3. A frontend service using root directory `/VirtueHire-main/Frontend`.

## Backend variables

Reference the variables from the Railway MySQL service:

```text
MYSQLHOST=${{MySQL.MYSQLHOST}}
MYSQLPORT=${{MySQL.MYSQLPORT}}
MYSQLDATABASE=${{MySQL.MYSQLDATABASE}}
MYSQLUSER=${{MySQL.MYSQLUSER}}
MYSQLPASSWORD=${{MySQL.MYSQLPASSWORD}}
```

Add application variables:

```text
MAIL_USERNAME=your-gmail-address
MAIL_PASSWORD=your-gmail-app-password
UPLOAD_DIR=/app/uploads
CORS_ALLOWED_ORIGIN_PATTERNS=https://admin.virtuehire.in,https://your-frontend-domain.up.railway.app
```

Attach a Railway volume to the backend at `/app/uploads`.

Production defaults avoid forcing a database connection during boot, so the
service can start even if Railway MySQL is still waking up. After your schema
exists, keep these defaults:

```text
DB_DDL_AUTO=none
DATABASE_SCHEMA_FIXER_ENABLED=false
```

For a first-time empty database only, temporarily deploy once with:

```text
DB_DDL_AUTO=update
HIBERNATE_ALLOW_JDBC_METADATA_ACCESS=true
```

Then set them back to the production values above.

For compiler functionality, also set:

```text
JUDGE0_BASE_URL=https://your-judge0-service
JUDGE0_API_KEY=your-api-key-if-required
```

## Frontend variables

Generate the backend public domain first, then set:

```text
REACT_APP_API_BASE_URL=https://your-backend-domain.up.railway.app/api
REACT_APP_WS_BASE_URL=https://your-backend-domain.up.railway.app
```

These values are embedded when the React frontend builds, so redeploy the frontend
after changing them.

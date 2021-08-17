# Migration from 1.1.0 to 1.2.0

## Database Changes

### Per-App APNs Environment Config

A new `ios_environment` column for the per-app APNs environment setting is introduced. The column can have the following values:

- `NULL` value (or any unknown string value) - The global configuration is applied (keeps the current behavior).
- `development` (case-insensitive) - The development environment is used for APNs. Use this value if the app is signed with development provisioning profile and distributed outside App Store and Testflight, i.e., via App Center.
- `production` (case-insensitive) - The production environment is used for APNs. Use for App Store or Testflight builds.

#### PostgreSQL

```sql
ALTER TABLE push_app_credentials ADD ios_environment VARCHAR(32) NULL;
```

#### Oracle

```sql
ALTER TABLE push_app_credentials ADD ios_environment VARCHAR2(32 CHAR) NULL;
```

#### MySQL

```sql
ALTER TABLE push_app_credentials ADD ios_environment VARCHAR(32) NULL;
```

### Storage of APNs Private Key (PostgreSQL)

A database column change is required for the PostgreSQL database due to larger APNs key sizes. The column type of `ios_private_key` in table `push_app_credentials` was changed from `VARCHAR(255)` to `BYTEA` to allow storing larger keys.

```sql
ALTER TABLE push_app_credentials ALTER COLUMN ios_private_key TYPE bytea USING ios_private_key::bytea;
```
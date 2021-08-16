# Migration from 1.1.0 to 1.2.0

## Database changes

A database column change is required for the PostgreSQL database due to larger APNs key sizes. The column type of `ios_private_key` in table `push_app_credentials` was changed from `VARCHAR(255)` to `BYTEA` to allow storing larger keys.

```sql
ALTER TABLE push_app_credentials ALTER COLUMN ios_private_key TYPE bytea USING ios_private_key::bytea;
```
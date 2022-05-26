# Migration from 1.2.0 to 1.3.0

## Database Changes

### Fixing Incorrect Table Type

In Oracle and Postgres databases, the `push_campaign_user.user_id` column used incorrect numeric type `INTEGER`/`NUMBER(19)` instead of `VARCHAR(255)`. To fix the issue, run the following scripts.

#### PostgreSQL

```sql
ALTER TABLE push_campaign_user ALTER COLUMN user_id TYPE VARCHAR(255) USING user_id::VARCHAR(255);
```

#### Oracle

```sql
ALTER TABLE push_campaign_user ALTER COLUMN user_id TYPE VARCHAR2(255 CHAR) USING user_id::VARCHAR2(255 CHAR);
```

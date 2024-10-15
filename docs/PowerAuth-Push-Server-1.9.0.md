# Migration from 1.8.x to 1.9.x

This guide contains instructions for migration from PowerAuth Push Server version `1.8.x` to version `1.9.x`.

## Database Changes

For convenience, you can use liquibase for your database migration.

If you prefer to make manual DB schema changes, please use the following SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.8.0_1.9.0.sql)
- [Oracle script](./sql/oracle/migration_1.8.0_1.9.0.sql)
- [MSSQL script](./sql/mssql/migration_1.8.0_1.9.0.sql)


### App Credentials Timestamp

To improve caching, the columns `timestamp_created`, and `timestamp_last_updated` have been added into the table `push_app_credentials`.

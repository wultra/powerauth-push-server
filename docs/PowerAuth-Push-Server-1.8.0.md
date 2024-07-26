# Migration from 1.7.x to 1.8.x

This guide contains instructions for migration from PowerAuth Push Server version `1.7.x` to version `1.8.x`.


## Database Changes

For convenience, you can use liquibase for your database migration.

**Important: Upgrading to version 1.8.x includes database column renaming required for the Inbox functionality, resulting in incompatible changes. Therefore, ensure all Push Server nodes are upgraded simultaneously during a scheduled service window.**

If you prefer to make manual DB schema changes, please use the following SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.7.0_1.8.0.sql)
- [Oracle script](./sql/oracle/migration_1.7.0_1.8.0.sql)
- [MSSQL script](./sql/mssql/migration_1.7.0_1.8.0.sql)
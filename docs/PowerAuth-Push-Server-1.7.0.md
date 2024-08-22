# Migration from 1.6.x to 1.7.x

This guide contains instructions for migration from PowerAuth Push Server version `1.6.x` to version `1.7.x`.


## Database Changes

For convenience, you can use liquibase for your database migration.

If you prefer to make manual DB schema changes, please use the following SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.6.0_1.7.0.sql)
- [Oracle script](./sql/oracle/migration_1.6.0_1.7.0.sql)
- [MSSQL script](./sql/mssql/migration_1.6.0_1.7.0.sql)


### Huawei Mobile Services

To support HMS, the columns `hms_project_id`, `hms_client_id`, and `hms_client_secret` have been added into the table `push_app_credentials`.

# Migration from 1.7.x to 1.8.x

This guide contains instructions for migration from PowerAuth Push Server version `1.7.x` to version `1.8.x`.

## Database Changes

Renamed columns in table `push_inbox` because of clashes with SQL language keywords:
- column `read` renamed to `is_read`
- column `type` renamed to `message_type`

You can use Liquibase to update the database structure to version `1.8.x`.

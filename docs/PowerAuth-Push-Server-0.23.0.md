# Migration from 0.22.0 to 0.23.0

## Database changes

Following DB changes occurred between version 0.22.0 and 0.23.0:
- Added unique index (`activationId`, `push_token`) in table `push_device_registration`
- The existing `push_device_activation` index in table `push_device_registration` was changed to unique

The DDL script for Oracle:
```sql
CREATE UNIQUE INDEX PUSH_DEVICE_ACTIVATION_TOKEN ON PUSH_DEVICE_REGISTRATION(ACTIVATION_ID, PUSH_TOKEN);

DROP INDEX PUSH_DEVICE_ACTIVATION;
CREATE UNIQUE INDEX PUSH_DEVICE_ACTIVATION ON PUSH_DEVICE_REGISTRATION(ACTIVATION_ID);
```

The DDL script for MySQL:
```sql
CREATE UNIQUE INDEX `push_device_activation_token` ON `push_device_registration`(`activation_id`, `push_token`);

DROP INDEX `push_device_activation` ON `push_device_registration`;
CREATE UNIQUE INDEX `push_device_activation` ON `push_device_registration`(`activation_id`);
```

In case either of the index updates fails, delete existing duplicate rows. Rows with newest timestamp_last_registered should be preserved.

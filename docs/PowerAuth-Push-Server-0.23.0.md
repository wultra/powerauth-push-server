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

## Device Registration Changes

Following changes of device registration have been applied in release `0.23.0`:
- A device can no longer be registered with the same `activationId` and multiple related `pushtoken` values. 
This change was introduced because Google and Apple do not always expire existing push tokens. When the device
receives a new push token, the device registration endpoint updates the `pushtoken` value for an existing activation instead of
creating a new device registration. Thus the old push token is removed from database.
- It is no longer possible to register a device without associated activation. The `activationId` parameter must be
always sent with device registration request.
- It is possible to re-register a device with same `activationId` and `pushtoken`. The registration timestamp is updated in this case. 
- It is possible to register a device with multiple `activationIds` associated with a single `pushtoken`. Such 
device registration must be initiated using the new endpoint created for this use case: 
[Create Device for Multiple Associated Activations](./Push-Server-API.md#create-device-for-multiple-associated-activations).
Multiple activations are used in master-child activation schemes. The functionality needs to be enabled
using a configuration property [for enabling multiple activations](./Deploying-Push-Server.md#enabling-multiple-associated-activations-in-device-registration),
because it is less secure than the case when a single associated activation is allowed for a push token.
- Database indexes are now applied to enforce database consistency for device registrations:
  - The (`activationId`) value must be unique in the device registration table. Each `activationId` must have exactly one associated `pushtoken`. 
  - The (`activationId`, `pushtoken`) combination must be unique in the device registration table.

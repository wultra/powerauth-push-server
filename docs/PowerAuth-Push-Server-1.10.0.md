# Migration from 1.9.x to 1.10.x

This guide contains instructions for migration from PowerAuth Push Server version `1.9.x` to version `1.10.x`.

## Database Changes

For convenience, you can use liquibase for your database migration.

If you prefer to make manual DB schema changes, please use the following SQL scripts:

- [PostgreSQL script](./sql/postgresql/migration_1.9.0_1.10.0.sql)
- [Oracle script](./sql/oracle/migration_1.9.0_1.10.0.sql)
- [MSSQL script](./sql/mssql/migration_1.9.0_1.10.0.sql)


### New Mobile Platform Enumerations

To better align with current development of mobile platforms, we deprecated original mobile platform definitions in favor of the mobile message service platforms:
- `iOS` -> `APNs`
- `Android` -> `FCM`
- `Huawei` -> `HMS`

The changes are reflected in the Push Server database:
- new APNs database columns in table `push_app_credentials`:
  - added column `apns_bundle`, value copied from column `ios_bundle`
  - added column `apns_environment`, value copied from column `ios_environment`
  - added column `apns_key_id`, value copied from column `ios_key_id`
  - added column `apns_private_key`, value copied from column `ios_private_key`
  - added column `apns_team_id`, value copied from column `ios_team_id`
- new FCM database columns in table `push_app_credentials`:
  - added column `fcm_private_key`, value copied from column `android_private_key`
  - added column `fcm_project_id`, value copied from column `android_project_id`

New endpoints were added into the REST API to migrate to new platform enumerations:
- `POST/PUT /admin/app/apns` - deprecating `POST/PUT /admin/app/ios/update`
- `DELETE /admin/app/apns` - deprecating `POST/DELETE /admin/app/ios/remove`
- `POST/PUT /admin/app/fcm` - deprecating `POST/PUT /admin/app/android/update`
- `DELETE /admin/app/fcm` - deprecating `POST/DELETE /admin/app/android/remove`
- `POST/PUT /admin/app/hms` - deprecating `POST/PUT /admin/app/huawei/update`
- `DELETE /admin/app/hms` - deprecating `POST/DELETE /admin/app/huawei/remove`

The application list endpoint `POST /admin/app/list` uses new platform enumerations in the response.

The unconfigured application list endpoint `POST /admin/app/unconfigured/list` uses new platform enumerations in the response.

The application detail endpoint `POST /admin/app/detail` uses new platform enumerations in both request and response.

The push message send endpoint `POST /push/message/send` contains new platform enumerations in the response for the count of sent messages. 

### Customization of APNs Environment per Registered Device

TODO
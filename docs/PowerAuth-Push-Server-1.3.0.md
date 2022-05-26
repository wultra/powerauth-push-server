# Migration from 1.2.0 to 1.3.0

## Database Changes

### Migrating Application ID

Push server now uses `String` application ID instead of numeric one. Therefore, you need to transfer the correct application name to the `push_app_credentials` table. In case you use the same schema to run PowerAuth Server and PowerAuth Push Server, you can use the following script to do it:

```sql
-- rename the original app_id column to app_id_orig 
ALTER TABLE push_app_credentials RENAME COLUMN app_id TO app_id_orig;

-- create a new column for the string app ID value
ALTER TABLE push_app_credentials ADD COLUMN IF NOT EXISTS app_id VARCHAR(255);

-- migrate the string app ID value from the PowerAuth table with applications
UPDATE push_app_credentials
SET app_id = pa.name
FROM (SELECT id, name FROM pa_application) AS pa
WHERE pa.id=push_app_credentials.app_id_orig;

-- remove the original column with numeric app ID value 
ALTER TABLE push_app_credentials DROP COLUMN app_id_orig;
```

Alternatively, you can alter the table manually to have `app_id` column of `VARCHAR(255)` type and value of PowerAuth application name.

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

### Spring Batch Tables

In case you would like to use campaign feature, you must make sure to create table for Spring Batch (in Spring Boot 2.x, this is not enabled by default). You can easily auto-create the required tables by setting the following property:

```
spring.batch.jdbc.initialize-schema=always
```

Alternatively, you can create the tables manually by following the Spring Batch documentation:

- [https://docs.spring.io/spring-boot/docs/2.0.0.M7/reference/htmlsingle/#howto-initialize-a-spring-batch-database](https://docs.spring.io/spring-boot/docs/2.0.0.M7/reference/htmlsingle/#howto-initialize-a-spring-batch-database)

## RESTful API Changes

The RESTful API now uses the string identifier of the PowerAuth application instead of a numeric one. As a result, wherever you relly on the embedded numeric identifier of the app, you should replace it by a string value.

Also, in several API calls, the `id` variable was renamed to `appId`, to maintain consistency. These calls are:

- Deleting the device
  - `POST /push/device/delete`
  - `DELETE /push/device/delete`
- Update iOS configuration for APNs
  - `POST /admin/app/ios/update`
  - `PUT /admin/app/ios/update`
- Remove iOS configuration for APNs
  - `POST /admin/app/ios/remove`
  - `DELETE /admin/app/ios/remove`
- Update Android configuration for APNs
  - `POST /admin/app/android/update`
  - `PUT /admin/app/android/update`
- Remove Android configuration for FCM
  - `POST /admin/app/android/remove`
  - `DELETE /admin/app/android/remove`
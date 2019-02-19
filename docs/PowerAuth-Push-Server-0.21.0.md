# Migration from 0.19.0 to 0.21.0

This guide contains instructions for migration from PowerAuth Push Server version 0.19.0 to version 0.21.0.

## Database changes

Following DB changes occurred between version 0.19.0 and 0.21.0:

## Updated database columns for FCM

* Table `push_app_credentials` - added column `android_private_key` which contains the Firebase service account private key used when obtaining access tokens for FCM HTTP v1 API.
* Table `push_app_credentials` - added column `android_project_id` which contains the Firebase project ID.
* Table `push_app_credentials` - dropped column `android_server_key` which was used by legacy FCM HTTP API.
* Table `push_app_credentials` - dropped column `android_bundle` which was replaced by `android_project_id`.

Migration scripts are available for Oracle and MySQL.

DB migration script for Oracle:
```sql
--
--  Updated columns for FCM
--

ALTER TABLE PUSH_APP_CREDENTIALS ADD ANDROID_PRIVATE_KEY BLOB DEFAULT NULL;
ALTER TABLE PUSH_APP_CREDENTIALS ADD ANDROID_PROJECT_ID VARCHAR2(255) DEFAULT NULL;
ALTER TABLE PUSH_APP_CREDENTIALS DROP COLUMN ANDROID_SERVER_KEY;
ALTER TABLE PUSH_APP_CREDENTIALS DROP COLUMN ANDROID_BUNDLE;
```

DB migration script for MySQL:
```sql
--
--   Updated columns for FCM
--

alter table push_app_credentials add `android_private_key` blob DEFAULT NULL;
alter table push_app_credentials add `android_project_id` varchar(255) DEFAULT NULL;
alter table push_app_credentials drop column `android_server_key`;
alter table push_app_credentials drop column `android_bundle`;
```

## Dropped database columns for legacy old end-to-end encryption support
DB migration script for Oracle and MySQL:
```sql
--
--   Dropped columns for legacy end-to-end encryption
--

ALTER TABLE PUSH_DEVICE_REGISTRATION DROP COLUMN ENCRYPTION_KEY;
ALTER TABLE PUSH_DEVICE_REGISTRATION DROP COLUMN ENCRYPTION_KEY_INDEX;

ALTER TABLE PUSH_MESSAGE DROP COLUMN IS_ENCRYPTED;
```

## Database indexes

You can apply following database indexes to improve database performance of Push Server.

The DDL script for Oracle:
```sql
CREATE UNIQUE INDEX PUSH_APP_CRED_APP ON PUSH_APP_CREDENTIALS(APP_ID);

CREATE INDEX PUSH_DEVICE_APP_TOKEN ON PUSH_DEVICE_REGISTRATION(APP_ID, PUSH_TOKEN);
CREATE INDEX PUSH_DEVICE_USER_APP ON PUSH_DEVICE_REGISTRATION(USER_ID, APP_ID);
CREATE INDEX PUSH_DEVICE_ACTIVATION ON PUSH_DEVICE_REGISTRATION(ACTIVATION_ID);

CREATE INDEX PUSH_MESSAGE_STATUS ON PUSH_MESSAGE(STATUS);

CREATE INDEX PUSH_CAMPAIGN_SENT ON PUSH_CAMPAIGN(IS_SENT);

CREATE INDEX PUSH_CAMPAIGN_USER_CAMPAIGN ON PUSH_CAMPAIGN_USER(CAMPAIGN_ID, USER_ID);
CREATE INDEX PUSH_CAMPAIGN_USER_DETAIL ON PUSH_CAMPAIGN_USER(USER_ID);
```

The DDL script for MySQL:
```sql
CREATE UNIQUE INDEX `push_app_cred_app` ON `push_app_credentials`(`app_id`);

CREATE INDEX `push_device_app_token` ON `push_device_registration`(`app_id`, `push_token`);
CREATE INDEX `push_device_user_app` ON `push_device_registration`(`user_id`, `app_id`);
CREATE INDEX `push_device_activation` ON `push_device_registration`(`activation_id`);

CREATE INDEX `push_message_status` ON `push_message`(`status`);

CREATE INDEX `push_campaign_sent` ON `push_campaign`(`is_sent`);

CREATE INDEX `push_campaign_user_campaign` ON `push_campaign_user`(`campaign_id`, `user_id`);
CREATE INDEX `push_campaign_user_detail` ON `push_campaign_user`(`user_id`);
```

## Storing of sent push messages disabled by default

Push server no longer stores sent push messages. You can enable storing of sent messages in database using following property:

```
powerauth.push.service.message.storage.enabled=true
```

## Configuration of Push service URL

Push server contains a new REST API which needs to be configured in case Push Server runs on non-standard port, non-standard context path or uses HTTPS. You can configure the service URL using following property:

```
powerauth.push.service.url=http://localhost:8080/powerauth-push-server
```

## Migration to FCM HTTP API v1

Push server started to use [FCM HTTP API v1](https://firebase.google.com/docs/cloud-messaging/migrate-v1) in version 0.21.0.

The configuration parameters have changed for FCM HTTP API v1:
- `Private key` needs to be configured instead of server key (which was removed from configuration).
- `Project ID` needs to be configured.

You can obtain both parameters from [Firebase Console](https://console.firebase.google.com). The project ID is visible in *Project Settings* | *General*. The private key can be generated using *Project Settings* | *Service Accounts* | *Firebase Admin SDK*, as described in [FCM documentation](https://firebase.google.com/docs/cloud-messaging/auth-server). Use the whole generated JSON file when configuring private key in Push server.


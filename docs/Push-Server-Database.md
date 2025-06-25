# Push Server Database

<!-- TEMPLATE database -->

PowerAuth Push Server requires several database tables in order to work.

You can download DDL scripts for supported databases:

- [PostgreSQL](./sql/postgresql/create_push_server_schema.sql)
- [Oracle](./sql/oracle/create_push_server_schema.sql)
- [MS SQL](./sql/mssql/create_push_server_schema.sql)

## Tables

<!-- begin remove -->
- [Push devices](#push-devices-table)
- [Push service credentials](#push-service-credentials-table)
- [Push messages](#push-messages-table)
- [Push campaigns](#push-campaigns-table)
- [Push campaign users](#push-campaign-users-table)
- [Message inbox](#message-inbox)
- [Message inbox mapping](#message-inbox-mapping-table)
<!-- end -->

<!-- begin database table push_device_registration -->
### Push Devices Table

Stores push tokens specific for a given device.

#### Columns

| Name                        | Type         | Info                              | Note                                                                                                                                  |
|-----------------------------|--------------|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `id`                        | INTEGER      | primary key, index, autoincrement | Unique device registration ID.                                                                                                        |
| `activation_id`             | VARCHAR(37)  | index                             | Activation ID associated with given push token record.                                                                                |
| `user_id`                   | INTEGER      | index                             | Associated user ID.                                                                                                                   |
| `app_id`                    | INTEGER      | index                             | Associated application ID.                                                                                                            |
| `platform`                  | VARCHAR(30)  | -                                 | Mobile OS Platform ("apns", "fcm", "hms", "ios" (deprecated), "android" (deprecated), "huawei" (deprecated).                          |
| `environment`               | VARCHAR(255) | -                                 | APNs environment - `development` or `production`, `null` value is used for other platforms.                                           |
| `push_token`                | VARCHAR(255) | -                                 | Push token associated with a given device. Type of the token is determined by the `platform` column.                                  |
| `timestamp_last_registered` | TIMESTAMP    | -                                 | Timestamp of the last device registration.                                                                                            |
| `is_active`                 | BOOLEAN      | -                                 | PowerAuth activation status (boolean), used as an activation status cache so that communication with PowerAuth Server can be minimal. |

#### Keys

| Name                            | Primary  | References | Description                   |
|---------------------------------|----------|------------|-------------------------------|
| `push_device_registration_pkey` | Y        | `id`       | Primary key for table records |

#### Indexes

| Name                           | Unique | Columns                     | Description                                                              |
|--------------------------------|--------|-----------------------------|--------------------------------------------------------------------------|
| `push_device_app_token`        | N      | `app_id, push_token`        | Index for faster lookup by push token for given app.                     |
| `push_device_user_app`         | N      | `user_id, app_id`           | Index for faster lookup by user ID for given app.                        |
| `push_device_activation`       | Y      | `activation_id`             | Index for faster lookup by activation ID.                                |
| `push_device_activation_token` | Y      | `activation_id, push_token` | Index for faster lookup by push token with constraints to activation ID. |
<!-- end -->

<!-- begin database table push_app_credentials -->
### Push Service Credentials Table

Stores per-app credentials used for communication with APNs / FCM.

#### Columns

| Name                     | Type         | Info                                 | Note                                                                                                                                                                                  |
|--------------------------|--------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `id`                     | INTEGER      | primary key, index, autoincrement    | Unique credential record ID.                                                                                                                                                          |
| `app_id`                 | VARCHAR(255) | index                                | Associated application ID.                                                                                                                                                            |
| `ios_key_id`             | VARCHAR(255) | -                                    | Key ID used for identifying a private key in APNs service (deprecated).                                                                                                               |
| `ios_private_key`        | BYTEA        | -                                    | Binary representation of P8 file with private key used for Apple's APNs service (deprecated).                                                                                         |
| `ios_team_id`            | VARCHAR(255) | -                                    | Team ID used for sending push notifications (deprecated).                                                                                                                             |
| `ios_bundle`             | VARCHAR(255) | -                                    | Application bundle ID, used as a APNs "topic" (deprecated).                                                                                                                           |
| `ios_environment`        | VARCHAR(32)  | -                                    | Per-application APNs environment setting. `NULL` or unknown value inherits from global server configuration, values `development` or `production` override the settings (deprecated). |
| `android_private_key`    | BYTEA        | -                                    | Firebase service account private key used when obtaining access tokens for FCM HTTP v1 API (deprecated).                                                                              |
| `android_project_id`     | VARCHAR(255) | -                                    | Firebase project ID, used when sending push messages using FCM (deprecated).                                                                                                          |
| `apns_key_id`            | VARCHAR(255) | -                                    | Key ID used for identifying a private key in APNs service.                                                                                                                            |
| `apns_private_key`       | BYTEA        | -                                    | Binary representation of P8 file with private key used for Apple's APNs service.                                                                                                      |
| `apns_team_id`           | VARCHAR(255) | -                                    | Team ID used for sending push notifications.                                                                                                                                          |
| `apns_bundle`            | VARCHAR(255) | -                                    | Application bundle ID, used as a APNs "topic".                                                                                                                                        |
| `apns_environment`       | VARCHAR(32)  | -                                    | Per-application APNs environment setting. `NULL` or unknown value inherits from global server configuration, values `development` or `production` override the settings.              |
| `fcm_private_key`        | BYTEA        | -                                    | Firebase service account private key used when obtaining access tokens for FCM HTTP v1 API.                                                                                           |
| `fcm_project_id`         | VARCHAR(255) | -                                    | Firebase project ID, used when sending push messages using FCM.                                                                                                                       |
| `hms_project_id`         | VARCHAR(255)        | -                                    | HMS project ID, used when sending push messages using HMS.                                                                                                                            |
| `hms_client_id`          | VARCHAR(255) | -                                    | HMS client ID credential value.                                                                                                                                                       |
| `hms_client_secret`      | VARCHAR(255) | -                                    | HMS client secret credential value.                                                                                                                                                   |
| `timestamp_created`      | TIMESTAMP    | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Timestamp when the record was created.                                                                                                                                                |
| `timestamp_last_updated` | TIMESTAMP    |                                      | Timestamp when the record was last updated.                                                                                                                                           |

#### Keys

| Name                        | Primary | References | Description                   |
|-----------------------------|---------|------------|-------------------------------|
| `push_app_credentials_pkey` | Y       | `id`       | Primary key for table records |

#### Indexes

| Name                | Unique | Columns  | Description                             |
|---------------------|--------|----------|-----------------------------------------|
| `push_app_cred_app` | Y      | `app_id` | Index for faster lookup by application. |
<!-- end -->

<!-- begin database table push_message -->
### Push Messages Table

Stores individual messages that were sent by the push server and their sent status.

#### Columns

| Name                     | Type        | Info                              | Note                                                                                                                      |
|--------------------------|-------------|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `id`                     | INTEGER     | primary key, index, autoincrement | Unique message record ID.                                                                                                 |
| `device_registration_id` | INTEGER     | index                             | Associated device registration (device that is used to receive the message), for the purpose of resend on fail operation. |
| `user_id`                | INTEGER     | index                             | Associated user ID.                                                                                                       |
| `activation_id`          | VARCHAR(37) | index                             | PowerAuth activation ID.                                                                                                  |
| `is_silent`              | BOOLEAN     | -                                 | Flag indicating if the message was "silent" (0 = NO, 1 = YES).                                                            |
| `is_personal`            | BOOLEAN     | -                                 | Flag indicating if the message was "personal" - sent only on active devices (0 = NO, 1 = YES).                            |
| `message_body`           | TEXT        | -                                 | Payload of the message in a unified server format. This format is later translated in a platform specific payload.        |
| `timestamp_created`      | TIMESTAMP   | -                                 | Date and time when the record was created.                                                                                |
| `status`                 | INTEGER     | -                                 | Value indicating message send status. (-1 = FAILED, 0 = PENDING, 1 = SENT).                                               |

#### Keys

| Name                | Primary | References | Description                   |
|---------------------|---------|------------|-------------------------------|
| `push_message_pkey` | Y       | `id`       | Primary key for table records |

#### Indexes

| Name                  | Unique | Columns  | Description                                             |
|-----------------------|--------|----------|---------------------------------------------------------|
| `push_message_status` | N      | `status` | Index for easier data split by the push sending status. |
<!-- end -->

<!-- begin database table push_campaign -->
### Push Campaigns Table

Stores particular campaigns together with notification messages.

#### Columns

| Name                  | Type      | Info                              | Note                                                          |
|-----------------------|-----------|-----------------------------------|---------------------------------------------------------------|
| `id`                  | INTEGER   | primary key, index, autoincrement | Unique campaign record ID.                                    |
| `app_id`              | INTEGER   | index                             | Associated Application identifier.                            |
| `message`             | TEXT      | -                                 | Certain notification that is written in unified format.       |
| `sent`                | BOOLEAN   | -                                 | Flag indicating if campaign was successfully sent.            |
| `timestamp_created`   | TIMESTAMP | -                                 | Timestamp of campaign creation.                               |
| `timestamp_sent`      | TIMESTAMP | -                                 | Timestamp of campaign sending initiation.                     |
| `timestamp_completed` | TIMESTAMP | -                                 | Timestamp of campaign successful sending (all messages sent). |

#### Keys

| Name                 | Primary | References | Description                   |
|----------------------|---------|------------|-------------------------------|
| `push_campaign_pkey` | Y       | `id`       | Primary key for table records |

#### Indexes

| Name                   | Unique | Columns   | Description                                                      |
|------------------------|--------|-----------|------------------------------------------------------------------|
| `push_campaign_sent`   | N      | `is_sent` | Index for easier data split by the push campaign sending status. |
<!-- end -->

<!-- begin database table push_campaign_user -->
### Push Campaign Users Table

Stores users who are going to get notification from specific campaign.

#### Columns

| Name                | Type      | Info                              | Note                                                                 |
|---------------------|-----------|-----------------------------------|----------------------------------------------------------------------|
| `id`                | INTEGER   | primary key, index, autoincrement | Unique user ID.                                                      |
| `campaign_id`       | INTEGER   | index                             | Identifier of campaign that is user related to.                      |
| `user_id`           | INTEGER   | index                             | Identifier of user, can occur multiple times in different campaigns. |
| `timestamp_created` | TIMESTAMP | -                                 | Timestamp of user creation (assignment to the campaign).             |

#### Keys

| Name                      | Primary | References | Description                   |
|---------------------------|---------|------------|-------------------------------|
| `push_campaign_user_pkey` | Y       | `id`       | Primary key for table records |

#### Indexes

| Name                          | Unique | Columns                | Description                                               |
|-------------------------------|--------|------------------------|-----------------------------------------------------------|
| `push_campaign_user_campaign` | N      | `campaign_id, user_id` | Index for easier campaign lookup for user by campaign ID. |
| `push_campaign_user_detail`   | N      | `user_id`              | Index for easier lookup by user ID.                       |
<!-- end -->

<!-- begin database table push_inbox -->
### Message Inbox

Stores the messages to be delivered to particular users.

#### Columns

| Name                | Type         | Info                              | Note                                            |
|---------------------|--------------|-----------------------------------|-------------------------------------------------|
| `id`                | INTEGER      | primary key, index, autoincrement | Unique message ID.                              |
| `inbox_id`          | INTEGER      | index                             | Identifier of message that is publicly visible. |
| `user_id`           | VARCHAR(255) | index                             | Identifier of user.                             |
| `type`              | VARCHAR(32)  | -                                 | Message type (`text` or `html`).                |
| `subject`           | TEXT         | -                                 | Message subject.                                |
| `summary`           | TEXT         | -                                 | Message summary.                                |
| `body`              | TEXT         | -                                 | Message body.                                   |
| `read`              | BOOLEAN      | index                             | Indication of in the message was read.          |
| `timestamp_created` | TIMESTAMP    | -                                 | Timestamp of message creation.                  |
| `timestamp_read`    | TIMESTAMP    | -                                 | Timestamp of when the message was read.         |

#### Keys

| Name            | Primary | References | Description                   |
|-----------------|---------|------------|-------------------------------|
| `push_inbox_pk` | Y       | `id`       | Primary key for table records |

#### Indexes

| Name                   | Unique | Columns           | Description                                                     |
|------------------------|---|-------------------|-----------------------------------------------------------------|
| `push_inbox_id`        | N | `inbox_id`        | Index for easier lookup for message by ID.                      |
| `push_inbox_user`      | N | `user_id`         | Index for easier lookup for message by user ID.                 |
| `push_inbox_user_read` | N | `user_id`, `read` | Index for easier lookup for message by user ID and read status. |
<!-- end -->

<!-- begin database table push_inbox -->
### Message Inbox Mapping Table

Stores the messages to application mapping.

#### Columns

| Name                 | Type       | Info | Note                        |
|----------------------|------------|------|-----------------------------|
| `app_credentials_id` | INTEGER    | PK   | Application credentials ID. |
| `inbox_id`           | INTEGER    | PK   | Unique message ID.          |

#### Keys

| Name                | Primary   | References                       | Description                   |
|---------------------|-----------|----------------------------------|-------------------------------|
| `push_inbox_app_pk` | Y         | `inbox_id`, `app_credentials_id` | Primary key for table records |

#### Indexes

| Name                   | Unique | Columns           | Description                                                     |
|------------------------|--------|-------------------|-----------------------------------------------------------------|
| `push_inbox_id`        | N      | `inbox_id`        | Index for easier lookup for message by ID.                      |
| `push_inbox_user`      | N      | `user_id`         | Index for easier lookup for message by user ID.                 |
| `push_inbox_user_read` | N      | `user_id`, `read` | Index for easier lookup for message by user ID and read status. |
<!-- end -->

## Sequences

<!-- begin database sequence push_credentials_seq -->
### Push Credentials Sequence

Sequence for application credentials registered in the system.

<!-- end -->

<!-- begin database sequence push_device_registration_seq -->
### Push Device Registration Sequence

Sequence for device registrations in the system.

<!-- end -->

<!-- begin database sequence push_message_seq -->
### Push Message Sequence

Sequence for push messages sent by the system.

<!-- end -->

<!-- begin database sequence push_campaign_seq -->
### Push Campaign Sequence

Sequence for push campaigns that are created in the system.

<!-- end -->

<!-- begin database sequence push_campaign_user_seq -->
### Push Campaign User Sequence

Sequence for user assignments to campaigns.

<!-- end -->

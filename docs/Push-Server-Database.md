# Push Server Database

PowerAuth Push Server requires several database tables in order to work.

You can download DDL scripts for supported databases:

- [Oracle - Create Database Schema](./sql/oracle/create_push_server_schema.sql)
- [MySQL - Create Database Schema](./sql/mysql/create_push_server_schema.sql)

## Tables

- [Push devices](#push-devices-table)
- [Push service credentials](#push-service-credentials-table)
- [Push messages](#push-messages-table)
- [Push campaigns](#push-campaigns-table)
- [Push campaign users](#push-campaign-users-table)
- [Push campaign devices](#push-campaign-devices-table)

### Push Devices Table


**Table name**: `push_device_registration`

**Purpose**: Stores push tokens specific for a given device.

**Columns**:

| Name | Type | Info | Note |
|---|---|---|---|
| `id` | BIGINT(20) | primary key, index, autoincrement | Unique device registration ID. |
| `activation_id` | VARCHAR(37) | index | Activation ID associated with given push token record. |
| `user_id` | BIGINT(20) | index | Associated user ID. |
| `app_id` | BIGINT(20) | index | Associated application ID. |
| `platform` | VARCHAR(30) | - | Mobile OS Platform ("ios", "android"). |
| `push_token` | VARCHAR(255) | - | Push token associated with a given device. Type of the token is determined by the `platform` column. |
| `timestamp_last_registered` | TIMESTAMP | - | Timestamp of the last device registration. |
| `is_active` | INT(11) | - | PowerAuth activation status (boolean), used as an activation status cache so that communication with PowerAuth Server can be minimal. |

### Push Service Credentials Table

**Table name**: `push_app_credentials`

**Purpose**: Stores per-app credentials used for communication with APNs / FCM.

**Columns**:

| Name | Type | Info | Note |
|---|---|---|---|
| id | BIGINT(20) | primary key, index, autoincrement | Unique credential record ID. |
| app_id | BIGINT(20) | index | Associated application ID. |
| ios_key_id | VARCHAR(255) | - | Key ID used for identifying a private key in APNs service. |
| ios_private_key | BLOB | - | Binary representation of P8 file with private key used for Apple's APNs service. |
| ios_team_id | VARCHAR(255) | - | Team ID used for sending push notifications. |
| ios_bundle | VARCHAR(255) | - | Application bundle ID, used as a APNs "topic". |
| android_private_key | BLOB | - | Firebase service account private key used when obtaining access tokens for FCM HTTP v1 API. |
| android_project_id | VARCHAR(255) | - | Firebase project ID, used when sending push messages using FCM. |

### Push Messages Table

**Table name**: `push_message`

**Purpose**: Stores individual messages that were sent by the push server and their sent status.

**Columns**:

| Name | Type | Info | Note |
|---|---|---|---|
| id | BIGINT(20) | primary key, index, autoincrement | Unique message record ID. |
| device_registration_id | INT | index | Associated device registration (device that is used to receive the message), for the purpose of resend on fail operation. |
| user_id | BIGINT(20) | index | Associated user ID. |
| activation_id | VARCHAR(37) | index | PowerAuth activation ID. |
| is_silent | INT | - | Flag indicating if the message was "silent" (0 = NO, 1 = YES). |
| is_personal | INT | - | Flag indicating if the message was "personal" - sent only on active devices (0 = NO, 1 = YES). |
| message_body | TEXT | - | Payload of the message in a unified server format. This format is later translated in a platform specific payload. |
| timestamp_created | TIMESTAMP | - | Date and time when the record was created. |
| status | INT | - | Value indicating message send status. (-1 = FAILED, 0 = PENDING, 1 = SENT). |

### Push Campaigns Table

**Table name**: `push_campaign`

**Purpose**: Stores particular campaigns together with notification messages

**Columns**:

| Name | Type | Info | Note |
|---|---|---|---|
| id | BIGINT(20) | primary key, index, autoincrement | Unique campaign record ID. |
| appid | BIGINT(20) | index | Associated Application identifier. |
| message | TEXT | - | Certain notification that is written in unified format. |
| sent| INT(1) | - | Flag indicating if campaign was successfully sent. |
| timestamp_created | TIMESTAMP | - | Timestamp of campaign creation. |
| timestamp_sent | TIMESTAMP | - | Timestamp of campaign sending initiation. |
| timestamp_completed | TIMESTAMP | - | Timestamp of campaign successful sending (all messages sent). |

### Push Campaign Users Table

**Table name**: `push_campaign_user`

**Purpose**: Stores users who are going to get notification from specific campaign

**Columns**:

| Name | Type | Info | Note |
|---|---|---|---|
| id | BIGINT(20) | primary key, index, autoincrement | Unique user ID. |
| campaign_id | BIGINT(20) | index | Identifier of campaign that is user related to. |
| user_id | BIGINT(20) | index | Identifier of user, can occur multiple times in different campaigns. |
| timestamp_created | TIMESTAMP | - | Timestamp of user creation (assignment to the campaign). |

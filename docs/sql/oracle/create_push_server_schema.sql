-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::1::Lubos Racansky
-- Create a new sequence push_credentials_seq
CREATE SEQUENCE push_credentials_seq START WITH 1 INCREMENT BY 1 CACHE 20;

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::2::Lubos Racansky
-- Create a new sequence sequence push_device_registration_seq
CREATE SEQUENCE push_device_registration_seq START WITH 1 INCREMENT BY 1 CACHE 20;

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::3::Lubos Racansky
-- Create a new sequence sequence push_message_seq
CREATE SEQUENCE push_message_seq START WITH 1 INCREMENT BY 1 CACHE 20;

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::4::Lubos Racansky
-- Create a new sequence sequence push_campaign_seq
CREATE SEQUENCE push_campaign_seq START WITH 1 INCREMENT BY 1 CACHE 20;

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::5::Lubos Racansky
-- Create a new sequence sequence push_campaign_user_seq
CREATE SEQUENCE push_campaign_user_seq START WITH 1 INCREMENT BY 1 CACHE 20;

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::6::Lubos Racansky
-- Create a new sequence sequence push_inbox_seq
CREATE SEQUENCE push_inbox_seq START WITH 1 INCREMENT BY 1 CACHE 20;

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::7::Lubos Racansky
-- Create a new sequence push_app_credentials
CREATE TABLE push_app_credentials (id INTEGER NOT NULL, app_id VARCHAR2(255) NOT NULL, ios_key_id VARCHAR2(255), ios_private_key BLOB, ios_team_id VARCHAR2(255), ios_bundle VARCHAR2(255), ios_environment VARCHAR2(32), android_private_key BLOB, android_project_id VARCHAR2(255), CONSTRAINT PK_PUSH_APP_CREDENTIALS PRIMARY KEY (id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::8::Lubos Racansky
-- Create a new sequence push_device_registration
CREATE TABLE push_device_registration (id INTEGER NOT NULL, activation_id VARCHAR2(37), user_id VARCHAR2(255), app_id INTEGER NOT NULL, platform VARCHAR2(255) NOT NULL, push_token VARCHAR2(255) NOT NULL, timestamp_last_registered TIMESTAMP(6) NOT NULL, is_active BOOLEAN, CONSTRAINT PK_PUSH_DEVICE_REGISTRATION PRIMARY KEY (id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::9::Lubos Racansky
-- Create a new sequence push_message
CREATE TABLE push_message (id INTEGER NOT NULL, device_registration_id INTEGER NOT NULL, user_id VARCHAR2(255) NOT NULL, activation_id VARCHAR2(37), is_silent BOOLEAN DEFAULT 0 NOT NULL, is_personal BOOLEAN DEFAULT 0 NOT NULL, message_body VARCHAR2(2048) NOT NULL, timestamp_created TIMESTAMP(6) NOT NULL, status INTEGER NOT NULL, CONSTRAINT PK_PUSH_MESSAGE PRIMARY KEY (id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::10::Lubos Racansky
-- Create a new sequence push_campaign
CREATE TABLE push_campaign (id INTEGER NOT NULL, app_id INTEGER NOT NULL, message VARCHAR2(4000) NOT NULL, is_sent BOOLEAN DEFAULT 0 NOT NULL, timestamp_created TIMESTAMP(6) NOT NULL, timestamp_sent TIMESTAMP(6), timestamp_completed TIMESTAMP(6), CONSTRAINT PK_PUSH_CAMPAIGN PRIMARY KEY (id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::11::Lubos Racansky
-- Create a new sequence push_campaign_user
CREATE TABLE push_campaign_user (id INTEGER NOT NULL, campaign_id INTEGER NOT NULL, user_id VARCHAR2(255) NOT NULL, timestamp_created TIMESTAMP(6) NOT NULL, CONSTRAINT PK_PUSH_CAMPAIGN_USER PRIMARY KEY (id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::12::Lubos Racansky
-- Create a new table push_inbox
CREATE TABLE push_inbox (id INTEGER NOT NULL, inbox_id VARCHAR2(37) NOT NULL, user_id VARCHAR2(255) NOT NULL, type VARCHAR2(32) NOT NULL, subject CLOB NOT NULL, summary CLOB NOT NULL, body CLOB NOT NULL, read BOOLEAN DEFAULT 0 NOT NULL, timestamp_created TIMESTAMP NOT NULL, timestamp_read TIMESTAMP, CONSTRAINT PK_PUSH_INBOX PRIMARY KEY (id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::13::Lubos Racansky
-- Create a new sequence push_inbox_app
CREATE TABLE push_inbox_app (app_credentials_id INTEGER NOT NULL, inbox_id INTEGER NOT NULL, CONSTRAINT PK_PUSH_INBOX_APP PRIMARY KEY (app_credentials_id, inbox_id));

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::14::Lubos Racansky
-- Create a new unique index on push_app_credentials(app_id)
CREATE UNIQUE INDEX push_app_cred_app ON push_app_credentials(app_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::15::Lubos Racansky
-- Create a new index on push_device_registration(app_id, push_token)
CREATE INDEX push_device_app_token ON push_device_registration(app_id, push_token);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::16::Lubos Racansky
-- Create a new index on push_device_registration(user_id, app_id)
CREATE INDEX push_device_user_app ON push_device_registration(user_id, app_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::17::Lubos Racansky
-- Create a new unique index on push_device_registration(activation_id)
CREATE UNIQUE INDEX push_device_activation ON push_device_registration(activation_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::18::Lubos Racansky
-- Create a new unique index on push_device_registration(activation_id, push_token)
CREATE UNIQUE INDEX push_device_activation_token ON push_device_registration(activation_id, push_token);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::19::Lubos Racansky
-- Create a new index on push_message(status)
CREATE INDEX push_message_status ON push_message(status);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::20::Lubos Racansky
-- Create a new index on push_campaign(is_sent)
CREATE INDEX push_campaign_sent ON push_campaign(is_sent);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::21::Lubos Racansky
-- Create a new index on push_campaign_user(campaign_id, user_id)
CREATE INDEX push_campaign_user_campaign ON push_campaign_user(campaign_id, user_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::22::Lubos Racansky
-- Create a new index on push_campaign_user(user_id)
CREATE INDEX push_campaign_user_detail ON push_campaign_user(user_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::23::Lubos Racansky
-- Create a new index on push_inbox(inbox_id)
CREATE INDEX push_inbox_id ON push_inbox(inbox_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::24::Lubos Racansky
-- Create a new index on push_inbox(user_id)
CREATE INDEX push_inbox_user ON push_inbox(user_id);

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::25::Lubos Racansky
-- Create a new index on push_inbox(user_id, read)
CREATE INDEX push_inbox_user_read ON push_inbox(user_id, read);

-- Changeset powerauth-push-server/1.4.x/20230322-add-tag-1.4.0.xml::1::Lubos Racansky
-- Changeset powerauth-push-server/1.5.x/20230905-add-tag-1.5.0.xml::1::Lubos Racansky
-- Changeset powerauth-push-server/1.7.x/20240119-push_app_credentials-hms.xml::1::Lubos Racansky
-- Add hms_project_id, hms_client_id, and hms_client_secret columns to push_app_credentials
ALTER TABLE push_app_credentials ADD hms_project_id VARCHAR2(255);

ALTER TABLE push_app_credentials ADD hms_client_id VARCHAR2(255);

ALTER TABLE push_app_credentials ADD hms_client_secret VARCHAR2(255);

COMMENT ON COLUMN push_app_credentials.hms_project_id IS 'Project ID defined in Huawei AppGallery Connect.';

COMMENT ON COLUMN push_app_credentials.hms_client_id IS 'Huawei OAuth 2.0 Client ID.';

COMMENT ON COLUMN push_app_credentials.hms_client_secret IS 'Huawei OAuth 2.0 Client Secret.';

-- Changeset powerauth-push-server/1.7.x/20240222-add-tag-1.7.0.xml::1::Lubos Racansky

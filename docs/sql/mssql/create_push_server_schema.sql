-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::1::Lubos Racansky
-- Create a new sequence push_credentials_seq
CREATE SEQUENCE push_credentials_seq START WITH 1 INCREMENT BY 1;
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::2::Lubos Racansky
-- Create a new sequence sequence push_device_registration_seq
CREATE SEQUENCE push_device_registration_seq START WITH 1 INCREMENT BY 1;
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::3::Lubos Racansky
-- Create a new sequence sequence push_message_seq
CREATE SEQUENCE push_message_seq START WITH 1 INCREMENT BY 1;
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::4::Lubos Racansky
-- Create a new sequence sequence push_campaign_seq
CREATE SEQUENCE push_campaign_seq START WITH 1 INCREMENT BY 1;
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::5::Lubos Racansky
-- Create a new sequence sequence push_campaign_user_seq
CREATE SEQUENCE push_campaign_user_seq START WITH 1 INCREMENT BY 1;
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::6::Lubos Racansky
-- Create a new sequence sequence push_inbox_seq
CREATE SEQUENCE push_inbox_seq START WITH 1 INCREMENT BY 1;
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::7::Lubos Racansky
-- Create a new sequence push_app_credentials
CREATE TABLE push_app_credentials (id int NOT NULL, app_id varchar(255) NOT NULL, ios_key_id varchar(255), ios_private_key varbinary(MAX), ios_team_id varchar(255), ios_bundle varchar(255), ios_environment varchar(32), android_private_key varbinary(MAX), android_project_id varchar(255), CONSTRAINT PK_PUSH_APP_CREDENTIALS PRIMARY KEY (id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::8::Lubos Racansky
-- Create a new sequence push_device_registration
CREATE TABLE push_device_registration (id int NOT NULL, activation_id varchar(37), user_id varchar(255), app_id int NOT NULL, platform varchar(255) NOT NULL, push_token varchar(255) NOT NULL, timestamp_last_registered datetime2(6) NOT NULL, is_active bit, CONSTRAINT PK_PUSH_DEVICE_REGISTRATION PRIMARY KEY (id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::9::Lubos Racansky
-- Create a new sequence push_message
CREATE TABLE push_message (id int NOT NULL, device_registration_id int NOT NULL, user_id varchar(255) NOT NULL, activation_id varchar(37), is_silent bit CONSTRAINT DF_push_message_is_silent DEFAULT 0 NOT NULL, is_personal bit CONSTRAINT DF_push_message_is_personal DEFAULT 0 NOT NULL, message_body varchar(2048) NOT NULL, timestamp_created datetime2(6) NOT NULL, status int NOT NULL, CONSTRAINT PK_PUSH_MESSAGE PRIMARY KEY (id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::10::Lubos Racansky
-- Create a new sequence push_campaign
CREATE TABLE push_campaign (id int NOT NULL, app_id int NOT NULL, message varchar(4000) NOT NULL, is_sent bit CONSTRAINT DF_push_campaign_is_sent DEFAULT 0 NOT NULL, timestamp_created datetime2(6) NOT NULL, timestamp_sent datetime2(6), timestamp_completed datetime2(6), CONSTRAINT PK_PUSH_CAMPAIGN PRIMARY KEY (id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::11::Lubos Racansky
-- Create a new sequence push_campaign_user
CREATE TABLE push_campaign_user (id int NOT NULL, campaign_id int NOT NULL, user_id varchar(255) NOT NULL, timestamp_created datetime2(6) NOT NULL, CONSTRAINT PK_PUSH_CAMPAIGN_USER PRIMARY KEY (id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::12::Lubos Racansky
-- Create a new table push_inbox
CREATE TABLE push_inbox (id int NOT NULL, inbox_id varchar(37) NOT NULL, user_id varchar(255) NOT NULL, type varchar(32) NOT NULL, subject varchar (max) NOT NULL, summary varchar (max) NOT NULL, body varchar (max) NOT NULL, [read] bit CONSTRAINT DF_push_inbox_read DEFAULT 0 NOT NULL, timestamp_created datetime2 NOT NULL, timestamp_read datetime2, CONSTRAINT PK_PUSH_INBOX PRIMARY KEY (id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::13::Lubos Racansky
-- Create a new sequence push_inbox_app
CREATE TABLE push_inbox_app (app_credentials_id int NOT NULL, inbox_id int NOT NULL, CONSTRAINT PK_PUSH_INBOX_APP PRIMARY KEY (app_credentials_id, inbox_id));
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::14::Lubos Racansky
-- Create a new unique index on push_app_credentials(app_id)
CREATE UNIQUE NONCLUSTERED INDEX push_app_cred_app ON push_app_credentials(app_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::15::Lubos Racansky
-- Create a new index on push_device_registration(app_id, push_token)
CREATE NONCLUSTERED INDEX push_device_app_token ON push_device_registration(app_id, push_token);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::16::Lubos Racansky
-- Create a new index on push_device_registration(user_id, app_id)
CREATE NONCLUSTERED INDEX push_device_user_app ON push_device_registration(user_id, app_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::17::Lubos Racansky
-- Create a new unique index on push_device_registration(activation_id)
CREATE UNIQUE NONCLUSTERED INDEX push_device_activation ON push_device_registration(activation_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::18::Lubos Racansky
-- Create a new unique index on push_device_registration(activation_id, push_token)
CREATE UNIQUE NONCLUSTERED INDEX push_device_activation_token ON push_device_registration(activation_id, push_token);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::19::Lubos Racansky
-- Create a new index on push_message(status)
CREATE NONCLUSTERED INDEX push_message_status ON push_message(status);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::20::Lubos Racansky
-- Create a new index on push_campaign(is_sent)
CREATE NONCLUSTERED INDEX push_campaign_sent ON push_campaign(is_sent);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::21::Lubos Racansky
-- Create a new index on push_campaign_user(campaign_id, user_id)
CREATE NONCLUSTERED INDEX push_campaign_user_campaign ON push_campaign_user(campaign_id, user_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::22::Lubos Racansky
-- Create a new index on push_campaign_user(user_id)
CREATE NONCLUSTERED INDEX push_campaign_user_detail ON push_campaign_user(user_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::23::Lubos Racansky
-- Create a new index on push_inbox(inbox_id)
CREATE NONCLUSTERED INDEX push_inbox_id ON push_inbox(inbox_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::24::Lubos Racansky
-- Create a new index on push_inbox(user_id)
CREATE NONCLUSTERED INDEX push_inbox_user ON push_inbox(user_id);
GO

-- Changeset powerauth-push-server/1.4.x/20230321-init-db.xml::25::Lubos Racansky
-- Create a new index on push_inbox(user_id, read)
CREATE NONCLUSTERED INDEX push_inbox_user_read ON push_inbox(user_id, [read]);
GO

-- Changeset powerauth-push-server/1.4.x/20230322-add-tag-1.4.0.xml::1::Lubos Racansky
-- Changeset powerauth-push-server/1.5.x/20230905-add-tag-1.5.0.xml::1::Lubos Racansky
-- Changeset powerauth-push-server/1.7.x/20240119-push_app_credentials-hms.xml::1::Lubos Racansky
-- Add hms_project_id, hms_client_id, and hms_client_secret columns to push_app_credentials
ALTER TABLE push_app_credentials ADD hms_project_id varchar(255);
GO

ALTER TABLE push_app_credentials ADD hms_client_id varchar(255);
GO

ALTER TABLE push_app_credentials ADD hms_client_secret varchar(255);
GO

IF EXISTS(  SELECT extended_properties.value FROM sys.extended_properties WHERE major_id = OBJECT_ID('dbo.push_app_credentials') AND name = N'MS_DESCRIPTION' AND minor_id = ( SELECT column_id FROM sys.columns WHERE name = 'hms_project_id' AND object_id = OBJECT_ID('dbo.push_app_credentials')) ) BEGIN  EXEC sys.sp_updateextendedproperty @name = N'MS_Description' , @value = N'Project ID defined in Huawei AppGallery Connect.' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'push_app_credentials' , @level2type = N'COLUMN' , @level2name = N'hms_project_id' END  ELSE  BEGIN  EXEC sys.sp_addextendedproperty @name = N'MS_Description' , @value = N'Project ID defined in Huawei AppGallery Connect.' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'push_app_credentials' , @level2type = N'COLUMN' , @level2name = N'hms_project_id' END;
GO

IF EXISTS(  SELECT extended_properties.value FROM sys.extended_properties WHERE major_id = OBJECT_ID('dbo.push_app_credentials') AND name = N'MS_DESCRIPTION' AND minor_id = ( SELECT column_id FROM sys.columns WHERE name = 'hms_client_id' AND object_id = OBJECT_ID('dbo.push_app_credentials')) ) BEGIN  EXEC sys.sp_updateextendedproperty @name = N'MS_Description' , @value = N'Huawei OAuth 2.0 Client ID.' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'push_app_credentials' , @level2type = N'COLUMN' , @level2name = N'hms_client_id' END  ELSE  BEGIN  EXEC sys.sp_addextendedproperty @name = N'MS_Description' , @value = N'Huawei OAuth 2.0 Client ID.' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'push_app_credentials' , @level2type = N'COLUMN' , @level2name = N'hms_client_id' END;
GO

IF EXISTS(  SELECT extended_properties.value FROM sys.extended_properties WHERE major_id = OBJECT_ID('dbo.push_app_credentials') AND name = N'MS_DESCRIPTION' AND minor_id = ( SELECT column_id FROM sys.columns WHERE name = 'hms_client_secret' AND object_id = OBJECT_ID('dbo.push_app_credentials')) ) BEGIN  EXEC sys.sp_updateextendedproperty @name = N'MS_Description' , @value = N'Huawei OAuth 2.0 Client Secret.' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'push_app_credentials' , @level2type = N'COLUMN' , @level2name = N'hms_client_secret' END  ELSE  BEGIN  EXEC sys.sp_addextendedproperty @name = N'MS_Description' , @value = N'Huawei OAuth 2.0 Client Secret.' , @level0type = N'SCHEMA' , @level0name = N'dbo' , @level1type = N'TABLE' , @level1name = N'push_app_credentials' , @level2type = N'COLUMN' , @level2name = N'hms_client_secret' END;
GO

-- Changeset powerauth-push-server/1.7.x/20240222-add-tag-1.7.0.xml::1::Lubos Racansky

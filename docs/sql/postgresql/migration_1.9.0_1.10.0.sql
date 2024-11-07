-- Changeset powerauth-push-server/1.10.x/20241029-add-new-platforms.xml::1::Roman Strobl
-- Add columns apns_private_key, apns_team_id, apns_key_id, apns_bundle, and apns_environment to push_app_credentials table
ALTER TABLE push_app_credentials ADD apns_private_key BYTEA;

ALTER TABLE push_app_credentials ADD apns_team_id VARCHAR(255);

ALTER TABLE push_app_credentials ADD apns_key_id VARCHAR(255);

ALTER TABLE push_app_credentials ADD apns_bundle VARCHAR(255);

ALTER TABLE push_app_credentials ADD apns_environment VARCHAR(255);

-- Changeset powerauth-push-server/1.10.x/20241029-add-new-platforms.xml::2::Roman Strobl
-- Add columns fcm_private_key and fcm_project_id to push_app_credentials table
ALTER TABLE push_app_credentials ADD fcm_private_key BYTEA;

ALTER TABLE push_app_credentials ADD fcm_project_id VARCHAR(255);

-- Changeset powerauth-push-server/1.10.x/20241029-migrate-ios-to-apns.xml::3::Roman Strobl
-- Migrate existing ios_* columns to apns_* columns
UPDATE push_app_credentials SET apns_bundle = ios_bundle, apns_environment = ios_environment, apns_key_id = ios_key_id, apns_private_key = ios_private_key, apns_team_id = ios_team_id;

-- Changeset powerauth-push-server/1.10.x/20241029-migrate-android-to-fcm.xml::4::Roman Strobl
-- Migrate existing android_* columns to fcm_* columns
UPDATE push_app_credentials SET fcm_private_key = android_private_key, fcm_project_id = android_project_id;

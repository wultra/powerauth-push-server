-- Changeset powerauth-push-server/1.9.x/20241011-app-credentials-timestamp.xml::1::Lubos Racansky
-- Add columns timestamp_last_updated and timestamp_created to push_app_credentials table
ALTER TABLE push_app_credentials ADD timestamp_created TIMESTAMP DEFAULT sysdate NOT NULL;

ALTER TABLE push_app_credentials ADD timestamp_last_updated TIMESTAMP;

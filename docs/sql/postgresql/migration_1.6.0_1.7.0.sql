-- Changeset powerauth-push-server/1.7.x/20240119-push_app_credentials-hms.xml::1::Lubos Racansky
-- Add hms_project_id, hms_client_id, and hms_client_secret columns to push_app_credentials
ALTER TABLE push_app_credentials ADD hms_project_id VARCHAR(255);

ALTER TABLE push_app_credentials ADD hms_client_id VARCHAR(255);

ALTER TABLE push_app_credentials ADD hms_client_secret VARCHAR(255);

COMMENT ON COLUMN push_app_credentials.hms_project_id IS 'Project ID defined in Huawei AppGallery Connect.';

COMMENT ON COLUMN push_app_credentials.hms_client_id IS 'Huawei OAuth 2.0 Client ID.';

COMMENT ON COLUMN push_app_credentials.hms_client_secret IS 'Huawei OAuth 2.0 Client Secret.';

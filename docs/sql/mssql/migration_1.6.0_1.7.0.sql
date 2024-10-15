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

-- Changeset powerauth-push-server/1.8.x/20240708-column-renaming-keywords.xml::1::Roman Strobl
-- Rename columns read to is_read and type to message_type in push_inbox table
exec sp_rename 'push_inbox.[read]', 'is_read', 'COLUMN';
GO

exec sp_rename 'push_inbox.type', 'message_type', 'COLUMN';
GO

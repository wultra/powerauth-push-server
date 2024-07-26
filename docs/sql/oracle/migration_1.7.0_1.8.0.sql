-- Changeset powerauth-push-server/1.8.x/20240708-column-renaming-keywords.xml::1::Roman Strobl
-- Rename columns read to is_read and type to message_type in push_inbox table
ALTER TABLE push_inbox RENAME COLUMN read TO is_read;

ALTER TABLE push_inbox RENAME COLUMN type TO message_type;

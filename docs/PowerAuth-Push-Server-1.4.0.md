# Migration from 1.3.x to 1.4.x

## New Message Inbox APIs

You can review and consider implementing our [Message Inbox API](./Push-Server-API.md). Message inbox is a new feature
of the push server that can be used to send targeted messages to the users to be read later.

## Database Changes

### Message Inbox Tables

Message inbox requires a simple database structure below:

#### PostgreSQL

```sql
CREATE TABLE push_inbox (
    id VARCHAR(37) CONSTRAINT push_inbox_pk PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    subject TEXT NOT NULL,
    body TEXT NOT NULL,
    read BOOLEAN DEFAULT false,
    timestamp_created TIMESTAMP NOT NULL,
    timestamp_read TIMESTAMP
);

CREATE INDEX push_inbox_user ON push_inbox (user_id);
CREATE INDEX push_inbox_user_read ON push_inbox (user_id, timestamp_created, read);
```

#### Oracle

```sql
CREATE TABLE PUSH_INBOX (
  ID VARCHAR2(37 CHAR) PRIMARY KEY,
  USER_ID VARCHAR2(255 CHAR) NOT NULL,
  SUBJECT BLOB NOT NULL,
  BODY BLOB NOT NULL,
  READ NUMBER(1) DEFAULT false,
  TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
  TIMESTAMP_READ TIMESTAMP(6)
);

CREATE INDEX PUSH_INBOX_USER ON push_inbox(USER_ID);
CREATE INDEX PUSH_INBOX_USER_READ ON push_inbox (USER_ID, TIMESTAMP_CREATED, READ);
```

#### MySQL

```sql
CREATE TABLE `push_inbox` (
  `id` VARCHAR(37),
  `user_id` VARCHAR(255) NOT NULL,
  `subject` TEXT NOT NULL,
  `body` TEXT NOT NULL,
  `read` BOOLEAN DEFAULT false,
  `timestamp_created` TIMESTAMP NOT NULL,
  `timestamp_read` TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX `push_inbox_user` ON `push_inbox` (`user_id`);
CREATE INDEX `push_inbox_user_read` ON `push_inbox` (`user_id`, `timestamp_created`, `read`);
```
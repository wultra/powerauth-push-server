# Migration from 1.3.x to 1.4.x

## New Message Inbox APIs

You can review and consider implementing our [Message Inbox API](./Push-Server-API.md#message-inbox). Message inbox is a new feature of the push server that can be used to send targeted messages to the users to be read later.

## Database Changes

### Message Inbox Tables

Message inbox requires a simple database structure below:

#### PostgreSQL

```sql
-- Create table for message inbox
CREATE TABLE push_inbox (
    id INTEGER NOT NULL CONSTRAINT push_inbox_pk PRIMARY KEY,
    inbox_id VARCHAR(37),
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    subject TEXT NOT NULL,
    summary TEXT NOT NULL,
    body TEXT NOT NULL,
    read BOOLEAN DEFAULT false NOT NULL,
    timestamp_created TIMESTAMP NOT NULL,
    timestamp_read TIMESTAMP
);

-- Create table for assignment of inbox messages to apps
CREATE TABLE push_inbox_app (
    app_credentials_id INTEGER NOT NULL,
    inbox_id           INTEGER NOT NULL,
    CONSTRAINT push_inbox_app_pk PRIMARY KEY (inbox_id, app_credentials_id)
);

CREATE INDEX push_inbox_id ON push_inbox (inbox_id);
CREATE INDEX push_inbox_user ON push_inbox (user_id);
CREATE INDEX push_inbox_user_read ON push_inbox (user_id, read);
```

#### Oracle

```sql
CREATE TABLE PUSH_INBOX (
  ID NUMBER(19) PRIMARY KEY NOT NULL,
  INBOX_ID VARCHAR2(37 CHAR),
  USER_ID VARCHAR2(255 CHAR) NOT NULL,
  TYPE VARCHAR2(32 CHAR),
  SUBJECT VARCHAR2(4000 CHAR) NOT NULL,
  SUMMARY VARCHAR2(4000 CHAR),
  BODY CLOB NOT NULL,
  READ NUMBER(1) DEFAULT 0 NOT NULL,
  TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
  TIMESTAMP_READ TIMESTAMP(6)
);

-- Create table for assignment of inbox messages to apps
CREATE TABLE PUSH_INBOX_APP (
  APP_CREDENTIALS_ID NUMBER(19) NOT NULL,
  INBOX_ID           NUMBER(19) NOT NULL,
  CONSTRAINT PUSH_INBOX_APP_PK PRIMARY KEY (INBOX_ID, APP_CREDENTIALS_ID)
);

CREATE INDEX PUSH_INBOX_ID ON PUSH_INBOX(INBOX_ID);
CREATE INDEX PUSH_INBOX_USER ON PUSH_INBOX(USER_ID);
CREATE INDEX PUSH_INBOX_USER_READ ON PUSH_INBOX(USER_ID, READ);
```

#### MySQL

```sql
CREATE TABLE `push_inbox` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inbox_id` VARCHAR(37),
  `user_id` VARCHAR(255) NOT NULL,
  `type` VARCHAR(32) NOT NULL,
  `subject` TEXT NOT NULL,
  `summary` TEXT NOT NULL,
  `body` TEXT NOT NULL,
  `read` BOOLEAN DEFAULT false NOT NULL,
  `timestamp_created` TIMESTAMP NOT NULL,
  `timestamp_read` TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create table for assignment of inbox messages to apps
CREATE TABLE `push_inbox_app` (
    `app_credentials_id` bigint(20) NOT NULL,
    `inbox_id`           bigint(20) NOT NULL,
    PRIMARY KEY (`inbox_id`, `app_credentials_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE INDEX `push_inbox_id` ON `push_inbox` (`inbox_id`);
CREATE INDEX `push_inbox_user` ON `push_inbox` (`user_id`);
CREATE INDEX `push_inbox_user_read` ON `push_inbox` (`user_id`, `read`);
```

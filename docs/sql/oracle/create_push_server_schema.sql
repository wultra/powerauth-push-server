CREATE SEQUENCE PUSH_CREDENTIALS_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PUSH_DEVICE_REGISTRATION_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PUSH_MESSAGE_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PUSH_CAMPAIGN_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PUSH_CAMPAIGN_USER_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE PUSH_INBOX_SEQ START WITH 1 INCREMENT BY 1;

CREATE TABLE PUSH_APP_CREDENTIALS
(
    ID NUMBER(19) PRIMARY KEY NOT NULL,
    APP_ID NUMBER(19) NOT NULL,
    IOS_KEY_ID VARCHAR2(255 CHAR),
    IOS_PRIVATE_KEY BLOB,
    IOS_TEAM_ID VARCHAR2(255 CHAR),
    IOS_BUNDLE VARCHAR2(255 CHAR),
    IOS_ENVIRONMENT VARCHAR2(32 CHAR),
    ANDROID_PRIVATE_KEY BLOB,
    ANDROID_PROJECT_ID VARCHAR2(255 CHAR)
);

CREATE TABLE PUSH_DEVICE_REGISTRATION
(
    ID NUMBER(19) PRIMARY KEY NOT NULL,
    ACTIVATION_ID VARCHAR2(37 CHAR),
    USER_ID VARCHAR2(255 CHAR),
    APP_ID NUMBER(19) NOT NULL,
    PLATFORM VARCHAR2(255 CHAR) NOT NULL,
    PUSH_TOKEN VARCHAR2(255 CHAR) NOT NULL,
    TIMESTAMP_LAST_REGISTERED TIMESTAMP(6) NOT NULL,
    IS_ACTIVE NUMBER(1)
);

CREATE TABLE PUSH_MESSAGE
(
    ID NUMBER(19) PRIMARY KEY NOT NULL,
    DEVICE_REGISTRATION_ID NUMBER(19) NOT NULL,
    USER_ID VARCHAR2(255 CHAR) NOT NULL,
    ACTIVATION_ID VARCHAR2(37 CHAR),
    IS_SILENT NUMBER(1) NOT NULL,
    IS_PERSONAL NUMBER(1) NOT NULL,
    MESSAGE_BODY VARCHAR2(2048 CHAR) NOT NULL,
    TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
    STATUS NUMBER(10) NOT NULL
);

CREATE TABLE PUSH_CAMPAIGN (
  ID NUMBER(19) PRIMARY KEY NOT NULL,
  APP_ID NUMBER(19) NOT NULL,
  MESSAGE VARCHAR2(4000 CHAR) NOT NULL,
  IS_SENT NUMBER(1) DEFAULT 0,
  TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
  TIMESTAMP_SENT  TIMESTAMP(6),
  TIMESTAMP_COMPLETED  TIMESTAMP(6)
);

CREATE TABLE PUSH_CAMPAIGN_USER (
  ID NUMBER(19) PRIMARY KEY NOT NULL,
  CAMPAIGN_ID NUMBER(19) NOT NULL,
  USER_ID VARCHAR2(255 CHAR) NOT NULL,
  TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL
);

CREATE TABLE PUSH_INBOX (
  ID NUMBER(19) PRIMARY KEY NOT NULL,
  INBOX_ID VARCHAR2(37 CHAR),
  USER_ID VARCHAR2(255 CHAR) NOT NULL,
  SUBJECT VARCHAR2(4000 CHAR) NOT NULL,
  BODY CLOB NOT NULL,
  READ NUMBER(1) DEFAULT 0 NOT NULL,
  TIMESTAMP_CREATED TIMESTAMP(6) NOT NULL,
  TIMESTAMP_READ TIMESTAMP(6)
);

---
--- Indexes for better performance.
---

CREATE UNIQUE INDEX PUSH_APP_CRED_APP ON PUSH_APP_CREDENTIALS(APP_ID);

CREATE INDEX PUSH_DEVICE_APP_TOKEN ON PUSH_DEVICE_REGISTRATION(APP_ID, PUSH_TOKEN);
CREATE INDEX PUSH_DEVICE_USER_APP ON PUSH_DEVICE_REGISTRATION(USER_ID, APP_ID);
CREATE UNIQUE INDEX PUSH_DEVICE_ACTIVATION ON PUSH_DEVICE_REGISTRATION(ACTIVATION_ID);
CREATE UNIQUE INDEX PUSH_DEVICE_ACTIVATION_TOKEN ON PUSH_DEVICE_REGISTRATION(ACTIVATION_ID, PUSH_TOKEN);

CREATE INDEX PUSH_MESSAGE_STATUS ON PUSH_MESSAGE(STATUS);

CREATE INDEX PUSH_CAMPAIGN_SENT ON PUSH_CAMPAIGN(IS_SENT);

CREATE INDEX PUSH_CAMPAIGN_USER_CAMPAIGN ON PUSH_CAMPAIGN_USER(CAMPAIGN_ID, USER_ID);
CREATE INDEX PUSH_CAMPAIGN_USER_DETAIL ON PUSH_CAMPAIGN_USER(USER_ID);

CREATE INDEX PUSH_INBOX_ID ON push_inbox(INBOX_ID);
CREATE INDEX PUSH_INBOX_USER ON push_inbox(USER_ID);
CREATE INDEX PUSH_INBOX_USER_READ ON push_inbox (USER_ID, READ);
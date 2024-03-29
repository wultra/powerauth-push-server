# Migration from 1.4.x to 1.5.x

This guide contains instructions for migration from PowerAuth Push Server version `1.4.x` to version `1.5.x`.

## Spring Boot 3

The PowerAuth Server was upgraded to Spring Boot 3, Spring Framework 6, and Hibernate 6.
It requires Java 17 or newer.

Remove this property.

`spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false`

Make sure that you use dialect without version.

```properties
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
# spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```

## New Asynchronous Sending Mode

When sending a push notification or batch of notifications, it is now possible to specify an additional `mode` parameter. The parameter specifies if the request processing should wait for sending all push notifications (`SYNCHRONOUS`, default value), or if the request processing should return value immediately (`ASYNCHRONOUS`). When `ASYNCHRONOUS` mode is specified, the response object does not include the number of sent, failed, pending and total messages.

### Synchronous Sending Example

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "mode": "SYNCHRONOUS",
    "message": {
      "userId": "test-user",
      "priority": "NORMAL",
      "body": {
        "title": "Some title",
        "body": "Some message body",
        "sound": "default"
      }
    }
  }
}
```

#### Response

```json
{
  "status": "OK",
  "responseObject": {
    "mode": "SYNCHRONOUS",
    "ios": {
      "sent": 1,
      "failed": 0,
      "pending": 0,
      "total": 1
    },
    "android": {
      "sent": 2,
      "failed": 0,
      "pending": 0,
      "total": 2
    }
  }
}
```

### Asynchronous Sending Example

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "mode": "ASYNCHRONOUS",
    "message": {
      "userId": "test-user",
      "priority": "NORMAL",
      "body": {
        "title": "Some title",
        "body": "Some message body",
        "sound": "default"
      }
    }
  }
}
```

#### Response

```json
{
  "status": "OK",
  "responseObject": {
    "mode": "ASYNCHRONOUS"
  }
}
```

## Database Changes


### Follow-up to Application ID Migrating

Migration guide to [PowerAuth Push Server 1.3.0](./PowerAuth-Push-Server-1.3.0.md) was not clear enough about changing application ID from number to string,
so you may miss the unique index `push_app_credentials(app_id)`.
Please double-check its presence and add it if needed.

```sql
-- reintroduce the unique index
CREATE UNIQUE INDEX push_app_cred_app ON push_app_credentials(app_id);
```


### Missing Inbox Constraint

Inbox table is missing _not null_ constraint for `inbox_id`.

### PostgreSQL

```sql
alter table push_inbox alter column inbox_id set not null;
```

### Oracle

```sql
alter table push_inbox modify inbox_id not null;
```


### Drop MySQL Support

Since version `1.5.0`, MySQL database is not supported anymore.


## Dependencies

PostgreSQL JDBC driver is already included in the WAR file.
Oracle JDBC driver remains optional and must be added to your deployment if desired.

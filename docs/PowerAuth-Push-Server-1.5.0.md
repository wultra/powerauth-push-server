# Migration from 1.4.x to 1.5.x

This guide contains instructions for migration from PowerAuth Push Server version `1.4.x` to version `1.5.x`.

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


### Drop MySQL Support

Since version `1.5.0`, MySQL database is not supported anymore.

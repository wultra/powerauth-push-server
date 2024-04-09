# Push Server API
<!-- template api -->

Push Server provides a RESTful API for the 3rd party integration purposes. The API contains methods related with:

- [Service](#service)
- [Device](#device)
- [Message](#message)
- [Campaign](#campaign)
- [Administration](#administration)
- [Message Inbox](#message-inbox)

Following endpoints are published in PowerAuth Push Server RESTful API:

<!-- begin remove -->
## Methods

##### **Request**
- Headers:
    - `Content-Type: application/json`
- required extensive details stored in `requestObject`

#### **Response**
- Status Code: `200`
- Headers:
    - `Content-Type: application/json`
- extensive details stored in `responseObject`

#### Device Management

- `POST` [/push/device/create](#create-device) - Create new device registration
- `POST` [/push/device/create/multi](#create-device-for-multiple-activations) - Create new device registration for multiple activations
- `POST` / `DELETE` [/push/device/delete](#delete-device) - Remove registered device
- `POST` / `PUT` [/push/device/status/update](#update-device-status) - Update the status of the activation so that when activation associated with given device is not active, no notifications are sent to the device.

#### Sending Push Messages

- `POST` [/push/message/send](#send-message) - Send single message to provided device
- `POST` [/push/message/batch/send](#send-message-batch) - Send message batch to multiple devices

#### Sending Campaign Notifications

- `POST` [/push/campaign/send/live/${id}](#send-campaign) - Send notifications to users from campaign
- `POST` [/push/campaign/send/test/${id}](#send-test-campaign) - Send notification to test users

#### Campaign Management

- `POST` [/push/campaign/create](#create-campaign) - Create new campaign
- `POST` / `DELETE` [/push/campaign/${ID}/delete](#delete-campaign) - Delete specific campaign
- `POST` / `DELETE` [/push/campaign/${ID}/user/delete](#delete-users-from-campaign) - Delete users from specific campaign
- `POST` / `PUT` [/push/campaign/${ID}/user/add](#add-users-to-campaign) - Add users to specific campaign
- `GET` [/push/campaign/${ID}/detail](#get-campaign) - Return specific campaign
- `GET` [/push/campaign/list?all={true,false}](#get-list-of-campaigns) - Return actual list of campaigns
- `GET` [/push/campaign/${ID}/user/list?page=${PAGE}&size=${SIZE}](#get-users-from-campaign) - Return paged list of users from specific campaign

#### Administration of Push Server

- `GET` [/admin/app/list](#list-applications) - List applications
- `GET` [/admin/app/unconfigured/list](#list-unconfigured-applications) - List unconfigured applications
- `POST` [/admin/app/detail](#application-detail) - Get application detail
- `POST` [/admin/app/create](#create-application) - Create application
- `POST` / `PUT` [/admin/ios/update](#update-ios-configuration) - Update iOS configuration
- `POST` / `DELETE` [/admin/ios/remove](#remove-ios-configuration) - Remove iOS configuration
- `POST` / `PUT` [/admin/android/update](#update-android-configuration) - Update Android configuration
- `POST` / `DELETE` [/admin/android/remove](#remove-android-configuration) - Remove Android configuration

#### Service Status

- `GET` [/push/service/status](#service) - Return status of service
<!-- end -->

### Error Handling

PowerAuth Push Server uses following format for error response body, accompanied with an appropriate HTTP status code. Besides the HTTP error codes that application server may return regardless of server application (such as 404 when resource is not found or 503 when server is down), following status codes may be returned:

| Status | HTTP Code | Description |
|--------|-----------|-------------|
| OK     | 200       | No issue    |
| ERROR  | 400       | Issue with a request format, or issue of the business logic |
| ERROR  | 401       | Unauthorized, invalid security token configuration |

All error responses that are produced by the PowerAuth Push Server have following body:

```json
{
  "status": "ERROR",
  "responseObject": {
    "code": "ERROR_GENERIC",
    "message": "Campaign with entered ID does not exist"
  }
}
```

- `status` - `OK`, `ERROR`
- `code` - `ERROR_GENERIC`, `ERROR_DATABASE`
- `message` - Message that describes certain error.

## Service

Describes basic information of application.

<!-- begin api GET /push/service/status -->
### Service Status

Send a system status response, with basic information about the running application.

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/service/status</td>
    </tr>
</table>
<!-- end -->

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "applicationName": "powerauth-push",
    "applicationDisplayName": "PowerAuth Push Server",
    "applicationEnvironment": "",
    "version": "0.21.0",
    "buildTime": "2019-01-22T14:59:14.954+0000",
    "timestamp": "2019-01-22T15:00:28.399+0000"
  }
}
```

- `applicationName` - Application name.
- `applicationDisplayName` - Application display name.
- `applicationEnvironment` - Application environment.
- `version` - Version of Push server.
- `buildTime` - Timestamp when the powerauth-push-server.war file was built.
- `timestamp` - Current time on application.
<!-- end -->

## Device

Represents mobile device with iOS, Android or Huawei that is capable to receive a push notification.
Device has to first register with APNS, FCM, or HMS to obtain push token.
Then it has to forward the push token to the push server end-point. After that push server is able to send push notification to the device.

<!-- begin api POST /push/device/create -->
### Create Device

Create a new device push token (platform specific). The call must include `activationId`, so that the token is associated with given user in the PowerAuth Server.

_Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activation ID, so that there are no incorrect bindings._

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/device/create</td>
    </tr>
</table>
<!-- end -->

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "token": "1234567890987654321234567890",
    "platform": "ios",
    "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412"
  }
}
```

- `appId` - Application that device is using.
- `token` - Identifier for device.
- `platform` - `ios`, `android`, `huawei`
- `activationId` - Activation identifier

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api POST /push/device/create/multi -->
### Create Device for Multiple Activations

Create a new device push token (platform specific). The call must include `activationIds` which contains list of activations to be associated with the registered device.

_Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activation IDs, so that there are no incorrect bindings._

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/device/create/multi</td>
    </tr>
</table>
<!-- end -->

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "token": "1234567890987654321234567890",
    "platform": "ios",
    "activationIds": [
      "49414e31-f3df-4cea-87e6-f214ca3b8412",
      "26c94bf8-f594-4bd8-9c51-93449926b644"
    ]
  }
}
```

- `appId` - Application that device is using.
- `token` - Identifier for device.
- `platform` - `ios`, `android`, `huawei`
- `activationIds` - Associated activation identifiers

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api DELETE /push/device/remove -->
### Remove Device

Removes registered device based on the push token value.

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/device/remove</td>
    </tr>
</table>
<!-- end -->

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "token": "12456789098321234567890"
  }
}
```

- `appId` - Application that device is using.
- `token` - Identifier for device.

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api POST /push/device/status/update -->
### Update Device Status

Update the status of given device registration based on the associated activation ID. This can help assure that registration is in non-active state and cannot receive personal messages.

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/device/status/update</td>
    </tr>
</table>
<!-- end -->

#### Request

```json
{
  "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412"
}
```

- `activationId` - Identifier of activation.

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

## Message

Represents a single notification sent to the device. It provides an abstraction of APNS or FCM message payload.

<!-- begin api POST /push/message/send -->
### Send Message

Send a single push message to given user via provided application, optionally to the specific device represented by given `activationId`.

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/message/send</td>
    </tr>
</table>
<!-- end -->

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "mode": "SYNCHRONOUS",
    "message": {
      "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412",
      "userId": "123",
      "priority": "HIGH",
      "attributes": {
        "personal": true,
        "silent": true
      },
      "body": {
        "title": "Balance update",
        "titleLocKey": "balance.update.title",
        "titleLocArgs": [],
        "body": "Your balance is now $745.00",
        "bodyLocKey": "balance.update.body",
        "bodyLocArgs": [],
        "badge": 3,
        "sound": "default",
        "icon": "custom-icon",
        "category": "balance-update",
        "collapseKey": "balance-update",
        "validUntil": "2017-12-11T21:22:29.923Z",
        "extras": {
          "_comment": "Any custom data."
        }
      }
    }
  }
}

```

- `appId` - Application that user/s is/are using.
- `mode` - Mode of sending, default value is `SYNCHRONOUS` (response waits for all messages being sent), you can use `ASYNCHRONOUS` (return response immediately).
- `message` - Body for notification creating.
    - `activationId` - Identifier of specific activation, that usually corresponds to the device.
    - `userId` - Identifier of user.
    - `priority` - Priority of the message, `NORMAL` or `HIGH` (default).
    - `attributes` - Set of boolean variables:
        - `silent` - Sent silent push notification (If _true_, no system UI is displayed).
        - `personal` - If _true_ and activation is not in `ACTIVE` state the message is not sent.
    - `body` - Body object is described in [here](./Push-Message-Payload-Mapping.md)
        - See [Push Message Payload Mapping](./Push-Message-Payload-Mapping.md) for details.

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "mode": "SYNCHRONOUS",
    "result": {
      "ios": {
        "sent": 1,
        "pending": 0,
        "failed": 0,
        "total": 1
      },
      "android": {
        "sent": 1,
        "pending": 0,
        "failed": 0,
        "total": 1
      },
      "huawei": {
        "sent": 1,
        "pending": 0,
        "failed": 0,
        "total": 1
      }
    }
  }
}
```

- `mode` - Used sending mode.
- `result` - Information about sending notifications. Present only for `SYNCHRONOUS` mode.
    - `sent` - Number of sent notifications.
    - `failed` - Number of failed notifications.
    - `pending` - Number of notifications that are still pending sending.
    - `total` - Number of total notifications.
<!-- end -->

<!-- begin api POST /push/message/batch/send  -->
### Send Message Batch

Sends a message batch - each item in the batch represents a message to given user. The message is sent via provided application (optionally to the specific device represented by given `activationId`).

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/message/batch/send</td>
    </tr>
</table>
<!-- end -->

#### Request

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "mode": "SYNCHRONOUS",
    "batch": [
      {
        "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412",
        "userId": "123",
        "priority": "HIGH",
        "attributes": {
          "personal": true,
          "silent": true
        },
        "body": {
          "title": "Balance update",
          "titleLocKey": "balance.update.title",
          "titleLocArgs": [],
          "body": "Your balance is now $745.00",
          "bodyLocKey": "balance.update.body",
          "bodyLocArgs": [],
          "badge": 3,
          "sound": "default",
          "icon": "custom-icon",
          "category": "balance-update",
          "collapseKey": "balance-update",
          "validUntil": "2017-12-11T21:22:29.923Z",
          "extras": {
            "_comment": "Any custom data."
          }
        }
      },
      {
        "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412",
        "userId": "1234",
        "priority": "HIGH",
        "attributes": {
          "personal": true,
          "silent": true
        },
        "body": {
          "title": "Balance update",
          "titleLocKey": "balance.update.title",
          "titleLocArgs": [],
          "body": "Your balance is now $745.00",
          "bodyLocKey": "balance.update.body",
          "bodyLocArgs": [],
          "badge": 3,
          "sound": "default",
          "icon": "custom-icon",
          "category": "balance-update",
          "collapseKey": "balance-update",
          "validUntil": "2017-12-11T21:22:29.923Z",
          "extras": {
            "_comment": "Any custom data."
          }
        }
      }
    ]
  }
}

```

- `appId` - Application that user is using.
- `mode` - Mode of sending, default value is `SYNCHRONOUS` (response waits for all messages being sent), you can use `ASYNCHRONOUS` (return response immediately).
- `batch` - List of messages, see [documentation for sending a single message](#send-message) for details.

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "mode": "SYNCHRONOUS",
    "result": {
      "ios": {
        "sent": 1,
        "pending": 0,
        "failed": 0,
        "total": 1
      },
      "android": {
        "sent": 1,
        "pending": 0,
        "failed": 0,
        "total": 1
      },
      "huawei": {
        "sent": 1,
        "pending": 0,
        "failed": 0,
        "total": 1
      }
    }
  }
}
```

- `mode` - Used sending mode.
- `result` - Information about sending notifications. Present only for `SYNCHRONOUS` mode.
  - `sent` - Number of sent notifications.
  - `failed` - Number of failed notifications.
  - `pending` - Number of notifications that are still pending sending.
  - `total` - Number of total notifications.
<!-- end -->

## Campaign

Used for informing closed group of users about some certain announcement containing message object described [here](./Push-Message-Payload-Mapping.md).

Furthermore, the campaign comes with:

- application that campaign is using
- timestamp of
- creation
- sending
- sent status - Whether is sent or not.
- devices - To prevent getting multiple messages on device. If there would be more than one user registered.

<!-- begin api POST /push/campaign/create -->
### Create Campaign

Create a campaign with application that campaign is using and certain message that contains parameters of message object.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/create</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "message": {
      "title": "Balance update",
      "titleLocKey": "balance.update.title",
      "titleLocArgs": [],
      "body": "Your balance is now $745.00",
      "bodyLocKey": "balance.update.body",
      "bodyLocArgs": [],
      "badge": 3,
      "sound": "default",
      "icon": "custom-icon",
      "category": "balance-update",
      "collapseKey": "balance-update",
      "validUntil": "2016-10-12T11:20:04Z",
      "extras": {
        "_comment": "Any custom data."
      }      
    }
  }
}
```

- `appId` - Identifier of application that campaign is using.
- `message` - parameters of message object are described [here](./Push-Message-Payload-Mapping.md).

_note: identifier of campaign is generated automatically_

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "123456789012345678901234567890"
  }
}
```

- `id` - Assigned ID to campaign.
<!-- end -->

<!-- begin api DELETE /push/campaign/${id}/delete -->
### Delete Campaign

Delete a specific campaign. Also users associated with this campaign are going to be deleted. If deletion was applied then deleted status is true.

<!-- begin remove -->
#### Request

<table>
    <tr>
        <td>Method</td>
        <td><code>POST / DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/${ID}/delete</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
</table>

##### Request Body

```json
{}
```

- empty request body

#### Response 200

```json
{
  "status": "OK",
  "responseObject" : {
    "deleted" : true
  }
}
```

- `deleted` - Indicate if deletion was applied.
<!-- end -->

<!-- begin api GET /push/campaign/${id}/detail -->
### Get Campaign

Return details of a specific campaign.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/${ID}/detail</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "10",
    "appId": "mobile-app",
    "sent": "false",
    "message": {
      "title": "Balance update",
      "titleLocKey": "balance.update.title",
      "titleLocArgs": [],
      "body": "Your balance is now $745.00",
      "bodyLocKey": "balance.update.body",
      "bodyLocArgs": [],
      "badge": 3,
      "sound": "default",
      "icon": "custom-icon",
      "category": "balance-update",
      "collapseKey": "balance-update",
      "validUntil": "2016-10-12T11:20:04Z",
      "extras": {
        "_comment": "Any custom data."
      }
    }
  }
}
```

- `id` - Identifier of campaign.
- `appId` - Identifier of application that campaign is using.
- `sent` - Indicator if campaign was sent.
- `message` - parameters of message object are described [here](./Push-Message-Payload-Mapping.md).
<!-- end -->

<!-- begin api GET /push/campaign/list -->
### Get List Of Campaigns

Return list of actually registered campaigns, based on `all` parameter. This parameter decides if return campaigns that are 'only sent'(statement _false_) or return all registered campaigns (statement _true_).

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/list/?all={true|false}</td>
    </tr>
</table>
<!-- end -->

#### Response 200

```json
{
  "status": "OK",
  "responseObject": [
    {
      "id": "10",
      "appId": "mobile-app",
      "sent": "false",
      "message": {
        "title": "Balance update",
        "titleLocKey": "balance.update.title",
        "titleLocArgs": [],
        "body": "Your balance is now $745.00",
        "bodyLocKey": "balance.update.body",
        "bodyLocArgs": [],
        "badge": 3,
        "sound": "default",
        "icon": "custom-icon",
        "category": "balance-update",
        "collapseKey": "balance-update",
        "validUntil": "2016-10-12T11:20:04Z",
        "extras": {
          "_comment": "Any custom data."
        }
      }
    }, {
      "id": "11",
      "appId": "mobile-app-other",
      "sent": "true",
      "message": {
        "title": "Balance update",
        "titleLocKey": "balance.update.title",
        "titleLocArgs": [],
        "body": "Your balance is now $300.00",
        "bodyLocKey": "balance.update.body",
        "bodyLocArgs": [],
        "badge": 3,
        "sound": "default",
        "icon": "custom-icon",
        "category": "balance-update",
        "collapseKey": "balance-update",
        "validUntil": "2017-10-12T11:20:04Z",
        "extras": {
          "_comment": "Any custom data."
        }
      }
    }
  ]
}
```
- array of campaigns
- `id` - Identifier of campaign.
- `appId` - Identifier of application that campaign is using.
- `sent` - Indicator if campaign was sent.
- `message` - parameters of message object are described [here](./Push-Message-Payload-Mapping.md).
<!-- end -->

<!-- begin api PUT /push/campaign/${id}/user/add -->
### Add Users To Campaign

Associate users to a specific campaign. Users are identified in request body as an array of strings.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/${ID}/user/add</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
</table>

##### Request Body

```json
{
  "requestObject": [
    "1234567890",
    "1234567891",
    "1234567893"
  ]
}
```

- list of users

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api GET /push/campaign/${ID}/user/list -->
### Get Users From Campaign

Return list users from a specific campaign. Users are shown in paginated format based on parameters assigned in URI.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/${ID}/user/list?page=${PAGE}&size=${SIZE}</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
    <tr>
        <td>page</td>
        <td>Number of page to show</td>
    </tr>
    <tr>
        <td>size</td>
        <td>Number of elements per page</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK",
  "page": 0,
  "size": 4,
  "responseObject": {
    "campaignId": "1234",
    "users": [
      "1234567890",
      "1234567892",
      "1234567893"
    ]
  }
}
```

- `page` - Actual page listed
- `size` - Chosen number of users per page
- `campaignId` - ID of a chosen campaign
- `users` - Array of users based on pagination parameters
<!-- end -->

<!-- begin api DELETE /push/campaign/${id}/user/delete -->
### Delete Users From Campaign

Delete users associated with a specific campaign. Users are identified request body as an array of strings.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/${ID}/user/delete</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
</table>

```json
{
  "requestObject": [
    "1234567890",
    "1234567891",
    "1234567893"
  ]
}
```

- list of users

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api POST /push/campaign/send/test/${id} -->
### Send Test Campaign Message

Send message from a specific campaign on test user to check rightness of that campaign.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/send/test/${ID}</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
</table>

##### Request Body

```json
{
  "requestObject": {
    "userId": "1234567890"
  }
}
```

- `userId` - ID of test user, usually "1234567890"

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api POST /push/campaign/send/live/${id} -->
### Send Campaign

Send message from a specific campaign to devices belonged to users associated with that campaign. Whereas each device gets a campaign only once.

If sending was successful then `sent` parameter is set on _true_ and `timestampSent` is set on current time.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/push/campaign/send/live/${ID}</td>
    </tr>
</table>
<!-- end -->

##### Query Parameters

<table>
    <tr>
        <td>id</td>
        <td>Campaign identifier</td>
    </tr>
</table>

- empty request body

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

## Administration

Services used for management of the push server instance.

<!-- begin api GET /admin/app/list -->
### List Applications

Get list of all applications.

<!-- begin remove -->
#### Request

<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/list</td>
    </tr>
</table>
<!-- end -->

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "applicationList": [
      {
        "appId": "mobile-app",
        "ios": true,
        "android": true,
        "huawei": true
      }
    ]
  }
}
```
<!-- end -->

<!-- begin api GET /admin/app/unconfigured/list -->
### List Unconfigured Applications

Get list of applications which have not been configured yet.

<!-- begin remove -->
#### Request

<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/unconfigured/list</td>
    </tr>
</table>
<!-- end -->

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "applicationList": [
      {
        "appId": "mobile-app-other",
        "ios": null,
        "android": null,
        "huawei": null
      }
    ]
  }
}
```
<!-- end -->

<!-- begin api POST /admin/app/detail -->
### Application Detail

Get detail of an application.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/detail</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "includeIos": true,
    "includeAndroid": true
  }
}
```

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "application": {
      "appId": "mobile-app",
      "ios": true,
      "android": true,
      "huawai": true
    },
    "iosBundle": "some.bundle.id",
    "iosKeyId": "KEYID123456",
    "iosTeamId": "TEAMID123456",
    "androidProjectId": "PROJECTID123",
    "huaweiProjectId": "HMS123"
  }
}
```
<!-- end -->

<!-- begin api POST /admin/app/create -->
### Create Application

Create a new supported application.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/create</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app"
  }
}
```

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "appId": "mobile-app"
  }
}
```
<!-- end -->

<!-- begin api PUT /admin/app/ios/update -->
### Update iOS Configuration

Update an iOS configuration.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/ios/update</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "bundle": "some.bundle.id",
    "keyId": "KEYID123456",
    "teamId": "TEAMID123456",
    "environment": null,
    "privateKeyBase64": "LS0tLS1CRUdJT..."
  }
}
```

You can use following `environment` values in request:
- `null` - environment is decided by configuration of server property `powerauth.push.service.apns.useDevelopment`
- `development` - use APNs development environment
- `production` - use APNs production environment

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api DELETE /admin/app/ios/remove -->
### Remove iOS Configuration

Remove an iOS configuration.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/ios/remove</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app"
  }
}
```

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api PUT /admin/app/android/update -->
### Update Android Configuration

Update an Android configuration.


#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/android/update</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "projectId": "PROJECTID123",
    "privateKeyBase64": "ewogICJ0eXBlIjog..."
  }
}
```

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api DELETE /admin/app/android/remove -->
### Remove Android Configuration

Remove FCM configuration for Android push messages.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/android/remove</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app"
  }
}
```

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api PUT /admin/app/huawei/update -->
### Update Huawei Configuration

Update an Huawei configuration.


#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/huawei/update</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app",
    "projectId": "PROJECTID123",
    "clientId": "oAuth 2.0 client ID",
    "clientSecret": "oAuth 2.0 client secret"
  }
}
```

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

<!-- begin api DELETE /admin/app/huawei/remove -->
### Remove Huawei Configuration

Remove configuration for Huawei push messages.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST / DELETE</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/admin/app/huawei/remove</td>
    </tr>
</table>
<!-- end -->

```json
{
  "requestObject": {
    "appId": "mobile-app"
  }
}
```

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

## Message Inbox

When communicating with your users, you can use the message inbox to store messages for users. Inbox is not responsible
for sending push notifications, but it is responsible for handling messages that should be read by the user later
and for marking the messages as read, while marking a timestamp.

Each message can have associated a type - either `text`, or `html`:

- For `text` messages, the layout of the final message is defined by `subject` and `body` attributes. The `summary`
  attribute is only used in list and push notifications.
- For `html` messages, the `subject` and `summary` attributes are only used in list and push notifications, the `body`
  attribute contains the full formatted HTML document. 

<!-- begin box info -->
To notify the user about a new message in the inbox, call the `POST /message/send` (or other push message sending API)
right after you post a message in the inbox.
<!-- end -->

<!-- begin api POST /inbox/messages -->
### Post Message to Inbox

Post provided message to an inbox of a given user.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>POST</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/inbox/messages</td>
    </tr>
</table>
<!-- end -->

##### Request Body

```json
{
  "requestObject": {
    "userId": "user-001",
    "type": "text",
    "subject": "Example subject",
    "summary": "Example summary",
    "body": "Example message body",
    "applications": [
      "application-001",
      "application-002"
    ]
  }
}
```

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "ae641389-d37a-4425-bd14-41c29484596f",
    "userId": "user-001",
    "type": "text",
    "subject": "Example subject",
    "summary": "Example summary",
    "body": "Example message body",
    "read": false,
    "timestampCreated": "2022-08-25T22:34:58.702+00:00",
    "applications": [
      "application-001",
      "application-002"
    ]
  }
}
```
<!-- end -->

<!-- begin api GET /inbox/messages/list -->
### Get User Messages

Get messages from an inbox of a given user.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/inbox/messages/list</td>
    </tr>
</table>
<!-- end -->

##### Query Params

<table>
    <tr>
        <td>userId</td>
        <td>User ID</td>
    </tr>
    <tr>
        <td>applications</td>
        <td>Applications, comma-separated value of application IDs.</td>
    </tr>
    <tr>
        <td>onlyUnread</td>
        <td>Set to true to return only unread messages.</td>
    </tr>
    <tr>
        <td>page</td>
        <td>Page index, zero based. Default: 0.</td>
    </tr>
    <tr>
        <td>size</td>
        <td>Page size. Default: 100.</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK",
  "page": 0,
  "size": 20,
  "responseObject": [
    {
      "id": "059b3a83-c025-45be-b815-497d39222024",
      "type": "text",
      "subject": "Hello, how are you?",
      "summary": "Example summary",
      "read": true,
      "timestampCreated": "2022-08-25T20:35:01.067+00:00"
    },
    {
      "id": "ae641389-d37a-4425-bd14-41c29484596f",
      "type": "text",
      "subject": "Example subject",
      "summary": "Example summary",
      "read": false,
      "timestampCreated": "2022-08-25T22:34:58.702+00:00"
    }
  ]
}
```
<!-- end -->

<!-- begin api GET /inbox/messages/count -->
### Get Count of Unread Messages

Get number of unread messages in an inbox of a given user.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/inbox/messages/count</td>
    </tr>
</table>
<!-- end -->

##### Query Params

<table>
    <tr>
        <td>userId</td>
        <td>User ID</td>
    </tr>
    <tr>
        <td>appId</td>
        <td>Application ID</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "countUnread": 1
  }
}
```
<!-- end -->

<!-- begin api GET /inbox/messages/detail -->
### Get Message Detail by ID

Get full message detail from an inbox for a given message ID.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>GET</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/inbox/messages/detail</td>
    </tr>
</table>
<!-- end -->

##### Query Params

<table>
    <tr>
        <td>messageId</td>
        <td>Message ID</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "ae641389-d37a-4425-bd14-41c29484596f",
    "userId": "user-001",
    "type": "text",
    "subject": "Example subject",
    "summary": "Example summary",
    "body": "Example message body",
    "read": false,
    "timestampCreated": "2022-08-25T22:34:58.702+00:00",
    "applications": [
      "application-001",
      "application-002"
    ]
  }
}
```
<!-- end -->

<!-- begin api PUT /inbox/messages/read -->
### Mark Message as Read

Mark a message in an inbox as read. In case the message is already read, this call is a no-op.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/inbox/messages/read</td>
    </tr>
</table>
<!-- end -->

##### Query Params

<table>
    <tr>
        <td>userId</td>
        <td>User ID</td>
    </tr>
    <tr>
        <td>messageId</td>
        <td>Message ID</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK",
  "responseObject": {
    "id": "ae641389-d37a-4425-bd14-41c29484596f",
    "userId": "user-001",
    "type": "text",
    "subject": "Example subject",
    "summary": "Example summary",
    "body": "Example message body",
    "read": true,
    "timestampCreated": "2022-08-25T22:34:58.702+00:00",
    "applications": [
      "application-001",
      "application-002"
    ]
  }
}
```
<!-- end -->

<!-- begin api PUT /inbox/messages/read-all -->
### Mark All Unread Messages as Read for User

Mark all unread messages in an inbox of a given user as read.

#### Request

<!-- begin remove -->
<table>
    <tr>
        <td>Method</td>
        <td><code>PUT</code></td>
    </tr>
    <tr>
        <td>Resource URI</td>
        <td>/inbox/messages/read-all</td>
    </tr>
</table>
<!-- end -->

##### Query Params

<table>
    <tr>
        <td>userId</td>
        <td>User ID</td>
    </tr>
    <tr>
        <td>appId</td>
        <td>Application ID</td>
    </tr>
</table>

#### Response 200

```json
{
  "status": "OK"
}
```
<!-- end -->

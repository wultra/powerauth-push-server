---
layout: page
title: Push Server API
---

Push Server provides a simple to use RESTful API for the 3rd party integration purposes. The API contains methods related with:

- [Service](#service)
- [Device](#device)
- [Message](#message)
- [Campaign](#campaign)

Following endpoints are published in PowerAuth 2.0 Push Server RESTful API:

## Methods

##### **Request**
- Headers:
    - `Content-Type: application/json`
- required extensive details stored in `requestObject`

#### **Response**
- Status Code: `200`
- Headers:
    - `Content-Type: application/json`
- extensive details stored in `responsetObject`

#### Device Management

- `POST` [/push/device/create](#create-device) - Create new device registration
- `POST` [/push/device/delete](#delete-device) - Remove registered device
- `POST` [/push/device/status/update](#update-device-status) - Update the status of the activation so that when activation associated with given device is not active, no notifications are sent to the device.

#### Sending Push Messages

- `POST` [/push/message/send](#send-message) - Send single message to provided device
- `POST` [/push/message/batch/send](#send-message-batch) - Send message batch to multiple devices

#### Sending Campaign Notifications

- `POST` [/push/campaign/send/live/${id}](#send-campaign) - Send notifications to users from campaign
- `POST` [/push/campaign/send/test/${id}](#send-test-campaign) - Send notification to test users

#### Campaign Management

- `POST` [/push/campaign/create](#create-campaign) - Create new campaign
- `POST` [/push/campaign/${ID}/delete](#delete-campaign) - Delete specific campaign
- `POST` [/push/campaign/${ID}/user/delete](#delete-users-from-campaign) - Delete users from specific campaign
- `PUT` [/push/campaign/${ID}/user/add](#add-users-to-campaign) - Add users to specific campaign
- `GET` [/push/campaign/${ID}/detail](#get-campaign) - Return specific campaign
- `GET` [/push/campaign/list/?all={true|false}](#get-list-of-campaigns) - Return actual list of campaigns
- `GET` [/push/campaign/${ID}/user/list?page=${PAGE}&size=${SIZE}](#get-users-from-campaign) - Return paged list of users from specific campaign

#### Service Status

- `GET` [/push/service/status](#service) - Return status of service

### Error Handling

PowerAuth 2.0 Push Server uses following format for error response body, accompanied with an appropriate HTTP status code. Besides the HTTP error codes that application server may return regardless of server application (such as 404 when resource is not found or 503 when server is down), following status codes may be returned:

|`status`|`HTTP code`       |Description|
|---     |---          |---|
|OK      |200          |No issue|
|ERROR   |400          |Issue with a request format, or issue of the business logic|
|ERROR   |401          | Unauthorized, invalid security token configuration|

All error responses that are produced by the PowerAuth 2.0 Push Server have following body:

```json

{
    "status": "ERROR",
    "responseObject": {
        "code": "ERROR_GENERIC",
        "message": "Campaign with entered ID does not exist"
    }
}
```

- `status` - _OK_ | _ERROR_
- `code` - _ERROR_GENERIC_ | _ERROR_DATABASE_
- `message` - Message that describes certain error.

## Service

Describes basic information of application.

### Service Status


Send a system status response, with basic information about the running application.

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

#### **Response**

```json
{
    "status": "OK",
    "responseObject": {
        "applicationName": "powerauth-push",
        "applicationDisplayName": "PowerAuth 2.0 Push Server",
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

## Device

Represents mobile device with iOS or Android that is capable to receive a push notification. Device has to first register with APNS or FCM to obtain push token.
Then it has to forward the push token to the push server end-point. After that push server is able to send push notification to the device.

### Create Device

Create a new device push token (platform specific). Optionally, the call may include also `activationId`, so that the token is associated with given user in the PowerAuth 2.0 Server.

_Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activationId value, so that there are no incorrect bindings._

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

#### **Request**

```json
{
    "requestObject": {
        "appId": 2,
        "token": "1234567890987654321234567890",
        "platform": "ios",
        "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412"
    }
}
```

- `appId` - Application that device is using.
- `token` - Identifier for device.
- `platform` - "_ios_ | _android_"
- `activationId` - Activation identifier

_Note: Activation ID is optional._

#### **Response**

```json
{
    "status": "OK"
}
```

### Delete Device

Removes registered device based on the push token value.

<table>
<tr>
<td>Method</td>
<td><code>POST</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/device/remove</td>
</tr>
</table>

#### **Request**

```json
{
    "requestObject": {
        "appId": 2,
        "token": "12456789098321234567890"
    }
}
```

- `appId` - Application that device is using.
- `token` - Identifier for device.

#### **Response**

```json
{
    "status": "OK"
}
```

### Update Device Status

Update the status of given device registration based on the associated activation ID. This can help assure that registration is in non-active state and cannot receive personal messages.

<table>
<tr>
<td>Method</td>
<td><code>POST</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/device/status/update</td>
</tr>
</table>

#### **Request**

```json
{
    "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412"
}
```

- `activationId` - Identifier of activation.

#### **Response**

```json
{
    "status": "OK"
}
```

## Message

Represents a single notification sent to the device. It provides an abstraction of APNS or FCM message payload.

### Send Message

Send a single push message to given user via provided application, optionally to the specific device represented by given `activationId`.

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

#### **Request**

```json
{
    "requestObject": {
        "appId": 2,
        "message": {
            "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412",
            "userId": "123",
            "attributes": {
                "personal": true,
                "silent": true
            },
            "body": {
                "title": "Balance update",
                "body": "Your balance is now $745.00",
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
- `message` - Body for notification creating.
    - `userId` - Identifier of user.
    - `activationId` - Identifier of specific activation, that usually corresponds to the device.
    - `attributes` - Set of boolean variables
        - `silent` - Sent silent push notification (If _true_, no system UI is displayed).
        - `personal` - If _true_ and activation is not in `ACTIVE` state the message is not sent.
    - `body` - Body object is described in [here](./Push-Message-Payload-Mapping.md)
        - See [Push Message Payload Mapping](./Push-Message-Payload-Mapping.md) for details.

#### **Response**

```json
{
    "status": "OK"
}
```

### Send Message Batch

Sends a message message batch - each item in the batch represents a message to given user. The message is sent via provided application (optionally to the specific device represented by given `activationId`).

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

#### **Request**

```json
{
    "requestObject": {
        "appId": 2,
        "batch": [
            {
                "activationId": "49414e31-f3df-4cea-87e6-f214ca3b8412",
                "userId": "123",
                "attributes": {
                    "personal": true,
                    "silent": true
                },
                "body": {
                    "title": "Balance update",
                    "body": "Your balance is now $745.00",
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
                "attributes": {
                    "personal": true,
                    "silent": true
                },
                "body": {
                    "title": "Balance update",
                    "body": "Your balance is now $745.00",
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

- `appId` - Application that user/s is/are using.
- `batch` - List of messages, see [documentation for sending a single message](#send-message) for details

#### **Response**

```json
{
    "status": "OK",
    "responseObject": {
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
            }
        }
    }
}
```

- `result` - Information about sending notifications.
- `sent` - Number of sent notifications.
- `failed` - Number of failed notifications.
- `total` - Number of total notifications.

## Campaign

Used for informing closed group of users about some certain announcement containing message object described [here](./Push-Message-Payload-Mapping.md).

Further campaign comes with:

- application that campaign is using
- timestamp of
- creation
- sending
- sent status - Whether is sent or not.
- devices - To prevent getting multiple messages on device. If there would be more than one user registered.

### Create Campaign

Create a campaign with application that campaign is using and certain message that contains parameters of message object.

#### **Request**
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

```json
{
    "requestObject": {
        "appId": "2",
        "message": {
            "title": "Balance update",
            "body": "Your balance is now $745.00",
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

#### **Response**

```json
{
    "status": "OK",
    "responseObject": {
        "id": "123456789012345678901234567890"
    }
}
```

- `id` - Assigned ID to campaign.

### Delete Campaign

Delete a specific campaign. Also users associated with this campaign are going to be deleted. If deletion was applied then deleted status is true.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>POST</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/${ID}/delete</td>
</tr>
<tr>
<td>Var ${ID} </td>
<td>Campaign identifier</td>
</tr>
</table>

```json
{

}
```

- empty request body

#### **Response**

```json
{
    "status": "OK",
    "responseObject" : {
        "deleted" : true
    }
}
```

- `deleted` - Indicate if deletion was applied.

### Get Campaign

Return details of a specific campaign.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>GET</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/${ID}/detail</td>
</tr>
<tr>
<td>Var ${ID} </td>
<td>Campaign identifier</td>
</tr>
</table>

#### **Response**

```json
{
    "status": "OK",
    "responseObject": {
        "id": "10",
        "appId": 2,
        "sent": "false",
        "message": {
            "title": "Balance update",
            "body": "Your balance is now $745.00",
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

### Get List Of Campaigns

Return list of actually registered campaigns, based on `all` parameter. This parameter decides if return campaigns that are 'only sent'(statement _false_) or return all registered campaigns (statement _true_).

#### **Request**
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

#### **Response**

``` json
{
    "status": "OK",
    "responseObject": [
        {
            "id": "10",
            "appId": 2,
            "sent": "false",
            "message": {
                "title": "Balance update",
                "body": "Your balance is now $745.00",
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
                "appId": 3,
                "sent": "true",
                "message": {
                    "title": "Balance update",
                    "body": "Your balance is now $300.00",
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

### Add Users To Campaign

Associate users to a specific campaign. Users are identified in request body as an array of strings.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>PUT</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/${ID}/user/add</td>
</tr>
<tr>
<td>Var ${ID} </td>
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

#### **Response**

```json
{
    "status": "OK"
}
```

### Get Users From Campaign

Return list users from a specific campaign. Users are shown in paginated format based on parameters assigned in URI.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>GET</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/${ID}/user/list?page=${PAGE}&size=${SIZE}</td>
</tr>
<tr>
<td>Var ${ID} </td>
<td>Campaign identifier</td>
</tr>
<tr>
<td>Var ${PAGE} </td>
<td>Nubmer of page to show</td>
</tr>
<tr>
<td>Var ${SIZE}</td>
<td>Number of elements per page</td>
</tr>
</table>

#### **Response**

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

### Delete Users From Campaign

Delete users associated with a specific campaign. Users are identified request body as an array of strings.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>POST</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/${ID}/user/delete</td>
</tr>
<tr>
<td>Var ${ID} </td>
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

#### **Response**

```json
{
    "status": "OK"
}
```

### Send Test Campaign

Send message from a specific campaign on test user to check rightness of that campaign.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>POST</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/send/test/${ID}</td>
<tr>
<td>Var ${ID} </td>
<td>Campaign identifier</td>
</tr>
</tr>
</table>

```json
{
    "requestObject": {
        "userId": "1234567890"
    }
}
```

- `userId` - ID of test user, usually "1234567890"

#### **Response**

```json
{
    "status": "OK"
}
```

### Send Campaign

Send message from a specific campaign to devices belonged to users associated with that campaign. Whereas each device gets a campaign only once.

If sending was successful then `sent` parameter is set on _true_ and `timestampSent` is set on current time.

#### **Request**
<table>
<tr>
<td>Method</td>
<td><code>POST</code></td>
</tr>
<tr>
<td>Resource URI</td>
<td>/push/campaign/send/live/${ID}</td>
</tr>
<tr>
<td>Var ${ID} </td>
<td>Campaign identifier</td>
</tr>
</table>

- empty request body

#### **Response**

```json
{
    "status": "OK"
}
```

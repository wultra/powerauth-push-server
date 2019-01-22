---
layout: page
title: Push Message Payload Mapping
---

The push server provides a convenient wrapper on top of the push messages sent to various platforms (APNS, FCM). This chapter describes what fields of the abstract push message are mapped to particular fields of APNS or FCM payload.

## Abstract Push Message Object

Abstract push message payload is represented using this object:

```java
public class PushMessageBody {

    private String title;
    private String body;
    private Integer badge;
    private String sound;
    private String icon;
    private String category;
    private String collapseKey;
    private Date validUntil;
    private Map<String, Object> extras;

}
```

## APNS Mapping

Attributes of the abstract push message object are mapped to APNS payload in following way:


| Abstract Message Attributes | APNS Mapped Attributes  |
|-----------------------------|-------------------------|
| `title`                     | `aps.alert.title`       |
| `body`                      | `aps.alert.body`        |
| `badge`                     | `aps.badge`             |
| `category`                  | `aps.category`          |
| `sound`                     | `aps.sound`             |
| `icon`                      | _ignored_               |
| `extras`                    | `[key:value]` custom data|
| `collapseKey`               | `aps.thread-id`         |
| `validUntil`                | Message expiration time on APNS servers (technical attribute), mapped to apns-expiration HTTP header.|

For the documentation of the APNS payload reference, please read the official Apple documentation:

- [Local and Remote Notification Programming Guide](https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/APNSOverview.html#//apple_ref/doc/uid/TP40008194-CH8-SW1)

### Silent Messages

In case a push message is marked with the `silent` flag, we add `content-available = 1` key to the `aps` payload.

## FCM Mapping

Attributes of the abstract push message object are mapped to FCM payload in following way:

| Abstract Message Attribute      | FCM Mapped Attributes |
|---------------------------------|------------------------|
| `title`                         | `notification.title`   |
| `body`                          | `notification.body`    |
| `badge`                         | _ignored_              |
| `category`                      | `notification.tag`     |
| `sound`                         | `notification.sound`   |
| `icon`                          | `notification.icon`
| `collapseKey`                   | `collapse_key`         |
| `validUntil`                    | _ignored_              |
| `extras`                        | `data`                 |

For the documentation of the FCM payload reference, please read the official Google documentation:

- [Cloud Messaging - About FCM Messages](https://firebase.google.com/docs/cloud-messaging/concept-options)

### Silent Messages

In case a push message is marked with the `silent` flag, we do not add attributes that trigger visible push notifications (attributes with `notification.*` path), even if they are present in the abstract push message object.

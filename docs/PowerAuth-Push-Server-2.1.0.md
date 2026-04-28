# Migration from 2.0.x to 2.1.x

No migration steps are necessary for update of Push Server version from `2.0.x` to `2.1.0`.

## Notification Badge Mapping

Previously, we only supported badges (the numbers displayed on an app icon) on the Apple platform. To unify the behavior on other mobile platforms, we mapped the `badge` value for FCM and HMS, too. See [Push Message Payload Mapping](./Push-Message-Payload-Mapping.md) for details of payload mapping.

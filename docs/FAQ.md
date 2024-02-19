# Frequently Asked Questions (FAQ)


## APNs

When iOS notifications do not work, double-check APNS configuration.
Mainly the host, the key, and the bundle.

APNs expose two hosts, production `api.push.apple.com` and development `api.sandbox.push.apple.com`.

The typical misconfiguration is the usage of the dev key or the dev bundle with the production host or vice versa.


## Global Configuration

Push Server uses [property powerauth.push.service.apns.useDevelopment](./Configuration-Properties#apns-configuration) configuring the host globally.
Value `false` represents the production environment.


## Application Configuration

The host may be overridden per application in the database table [push_app_credentials.ios_environment](./Push-Server-Database#push-service-credentials-table).

You may use the following values in the column `ios_environment`:

- `null` - environment is decided by the configuration of server property `powerauth.push.service.apns.useDevelopment`
- `development` - use APNs development environment
- `production` - use APNs production environment

It is possible to set this value via REST API [PUT /admin/app/ios/update](./Push-Server-API#update-ios-configuration) in the attribute `environment`.
Or see `curl` command example at [Update APNs Configuration](./Push-Server-Administration#update-apns-configuration).


## Application Logs

The chosen host is logged at INFO level as a message starting with the prefix `Using APNs`.

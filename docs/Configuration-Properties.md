# Configuration Properties

The Push Server uses the following public configuration properties:

## Database Configuration

| Property | Default | Note |
|---|---|---|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/powerauth` | Database JDBC URL |
| `spring.datasource.username` | `powerauth` | Database JDBC username |
| `spring.datasource.password` | `_empty_` | Database JDBC password |
| `spring.jpa.properties.hibernate.connection.characterEncoding` | `utf8` | Character encoding |
| `spring.jpa.properties.hibernate.connection.useUnicode` | `true` | Character encoding - Unicode support |

##  PowerAuth Service Configuration

| Property                                             | Default                                            | Note                                                                                                   |
|------------------------------------------------------|----------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `powerauth.service.url`                              | `http://localhost:8080/powerauth-java-server/rest` | PowerAuth service REST API base URL                                                                    |
| `powerauth.service.restClientConfig.responseTimeout` | `60s`                                              | PowerAuth REST API response timeout.                                                                   |
| `powerauth.service.restClientConfig.maxIdleTime`     | `200s`                                             | PowerAuth REST API max idle time.                                                                      |
| `powerauth.service.security.clientToken`             | `_empty_`                                          | PowerAuth REST API authentication token                                                                | 
| `powerauth.service.security.clientSecret`            | `_empty_`                                          | PowerAuth REST API authentication secret / password                                                    |
| `powerauth.service.ssl.acceptInvalidSslCertificate`  | `false`                                            | Flag indicating if connections using untrusted TLS certificate should be made to the PowerAuth Service |

## PowerAuth Push Service Configuration

| Property                                                          | Default                 | Note                                                                                                                                                                    |
|-------------------------------------------------------------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `powerauth.push.service.applicationName`                          | `powerauth-push`        | Technical name of the instance                                                                                                                                          |
| `powerauth.push.service.applicationDisplayName`                   | `PowerAuth Push Server` | Display name of the instance                                                                                                                                            |
| `powerauth.push.service.applicationEnvironment`                   | `_empty_`               | Environment identifier                                                                                                                                                  |
| `powerauth.push.service.message.storage.enabled`                  | `false`                 | Whether persistent storing of sent messages is enabled                                                                                                                  | 
| `powerauth.push.service.registration.multipleActivations.enabled` | `false`                 | Whether push registration supports "associated activations"                                                                                                             |
| `powerauth.push.service.registration.retry.backoff`               | `100`                   | Duration in milliseconds before a retry attempt during device registration in case of an insert error                                                                   |
| `powerauth.push.service.registration.retry.maxAttempts`           | `2`                     | Max number of retry attempts during device registration in case of an insert error                                                                                      |
| `powerauth.push.service.clients.cache.refreshAfterWrite`          | `5m`                    | APNS, FCM and HMS client configuration is cached. It is evicted if updated via administration on a single node. This is a smart fallback for the clustered environment. |

## PowerAuth Push Campaign Setup

| Property | Default | Note |
|---|---|---|
| `powerauth.push.service.campaign.batchSize` | `100000` | Default batch size for a campaign sending |

## Certificate Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.push.java.cacerts.password` | `changeit` | Java security CA certs file password |

## APNs Configuration

| Property | Default     | Note |
|---|-------------|---|
| `powerauth.push.service.apns.useDevelopment` | `true`      | Flag indicating that the development instance of APNS service should be used |
| `powerauth.push.service.apns.proxy.enabled` | `false`     | Flag indicating if the communication needs to go through proxy |
| `powerauth.push.service.apns.proxy.host` | `127.0.0.1` | Proxy host |
| `powerauth.push.service.apns.proxy.port` | `8080`      | Proxy port |
| `powerauth.push.service.apns.proxy.username` | `_empty_`   | Proxy username | 
| `powerauth.push.service.apns.proxy.password` | `_empty_`   | Proxy password |
| `powerauth.push.service.apns.connect.timeout` | `5000`      | Push message gateway connect timeout in milliseconds |
| `powerauth.push.service.apns.idlePingInterval` | `60000`     | Interval in milliseconds specifying the frequency of APNS ping calls in idle state |
| `powerauth.push.service.apns.concurrentConnections` | `1`         | Push message concurrency settings |

## FCM Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.push.service.fcm.proxy.enabled` | `false` | Flag indicating if the communication needs to go through proxy |
| `powerauth.push.service.fcm.proxy.host` | `127.0.0.1` | Proxy host |
| `powerauth.push.service.fcm.proxy.port` | `8080` | Proxy port |
| `powerauth.push.service.fcm.proxy.username` | `_empty_` | Proxy username | 
| `powerauth.push.service.fcm.proxy.password` | `_empty_` | Proxy password |
| `powerauth.push.service.fcm.dataNotificationOnly` | `false` | Flag indicating that FCM service should never use "notification" format, only a data format with extra payload representing the notification |
| `powerauth.push.service.fcm.sendMessageUrl` | `https://fcm.googleapis.com/v1/projects/%s/messages:send` | Default URL for the FCM service |
| `powerauth.push.service.fcm.connect.timeout` | `5000` | Push message gateway connect timeout in milliseconds | 

## HMS Configuration

| Property                                          | Default                                                 | Note                                                                                                                                          |
|---------------------------------------------------|---------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `powerauth.push.service.hms.proxy.enabled`        | `false`                                                 | Flag indicating if the communication needs to go through proxy.                                                                               |
| `powerauth.push.service.hms.proxy.host`           | `127.0.0.1`                                             | Proxy host.                                                                                                                                   |
| `powerauth.push.service.hms.proxy.port`           | `8080`                                                  | Proxy port.                                                                                                                                   |
| `powerauth.push.service.hms.proxy.username`       | `_empty_`                                               | Proxy username.                                                                                                                               | 
| `powerauth.push.service.hms.proxy.password`       | `_empty_`                                               | Proxy password.                                                                                                                               |
| `powerauth.push.service.hms.dataNotificationOnly` | `false`                                                 | Flag indicating that HMS service should never use "notification" format, only a data format with extra payload representing the notification. |
| `powerauth.push.service.hms.sendMessageUrl`       | `https://push-api.cloud.huawei.com/v2/%s/messages:send` | Default URL for the HMS service.                                                                                                              |
| `powerauth.push.service.hms.tokenUrl`             | `https://oauth-login.cloud.huawei.com/oauth2/v3/token`  | Default URL for the HMS OAuth service to obtain an access token.                                                                              |
| `powerauth.push.service.hms.connect.timeout`      | `5s`                                                    | Push message gateway connect timeout.                                                                                                         | 
| `powerauth.push.service.hms.response.timeout`     | `60s`                                                   | Push message gateway maximum duration allowed between each network-level read operations.                                                     | 
| `powerauth.push.service.hms.max-idle-time`        | `200s`                                                  | Push message gateway ConnectionProvider max idle time.                                                                                        | 

## Correlation HTTP Header Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.service.correlation-header.enabled` | `false` | Whether correlation header is enabled |
| `powerauth.service.correlation-header.name` | `X-Correlation-ID` | Correlation header name |
| `powerauth.service.correlation-header.value.validation-regexp` | `[a-zA-Z0-9\\-]{8,1024}` | Regular expression for correlation header value validation |
| `logging.pattern.console` | [See application.properties](https://github.com/wultra/powerauth-push-server/blob/develop/powerauth-push-server/src/main/resources/application.properties#docucheck-keep-link) | Logging pattern for console which includes the correlation header value |


## Monitoring and Observability

| Property                                  | Default | Note                                                                                                                                                                        |
|-------------------------------------------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `management.tracing.sampling.probability` | `1.0`   | Specifies the proportion of requests that are sampled for tracing. A value of 1.0 means that 100% of requests are sampled, while a value of 0 effectively disables tracing. |

The WAR file includes the `micrometer-registry-prometheus` dependency.
Discuss its configuration with the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/3.1.x/reference/html/actuator.html#actuator.metrics).

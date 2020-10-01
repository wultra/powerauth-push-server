# Configuration Properties

The Push Server uses the following public configuration properties:

## Database Configuration

| Property | Default | Note |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/powerauth` | Database JDBC URL |
| `spring.datasource.username` | `powerauth` | Database JDBC username |
| `spring.datasource.password` | `_empty_` | Database JDBC passwod |
| `spring.datasource.driver-class-name` | `com.mysql.jdbc.Driver` | Datasource JDBC class name | 
| `spring.jpa.properties.hibernate.connection.characterEncoding` | `utf8` | Character encoding |
| `spring.jpa.properties.hibernate.connection.useUnicode` | `true` | Character encoding - Unicode support |


##  PowerAuth Service Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.service.url` | `http://localhost:8080/powerauth-java-server/rest` | PowerAuth service REST API base URL | 
| `powerauth.service.security.clientToken` | `_empty_` | PowerAuth REST API authentication token | 
| `powerauth.service.security.clientSecret` | `_empty_` | PowerAuth REST API authentication secret / password |
| `powerauth.service.ssl.acceptInvalidSslCertificate` | `false` | Flag indicating if connections using untrusted TLS certificate should be made to the PowerAuth Service |

## PowerAuth Push Service Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.push.service.applicationName` | `powerauth-push` | Technical name of the instance |
| `powerauth.push.service.applicationDisplayName` | `PowerAuth Push Server` | Display name of the instance |
| `powerauth.push.service.applicationEnvironment` | `_empty_` | Environment identifier |
| `powerauth.push.service.message.storage.enabled` | `false` | Whether persistent storing of sent messages is enabled | 
| `powerauth.push.service.registration.multipleActivations.enabled` | `false` | Whether push registration supports "associated activations." |

## PowerAuth Push Campaign Setup

| Property | Default | Note |
|---|---|---|
| `powerauth.push.service.campaign.batchSize` | `100000` | Default batch size for a campaign sending. |


## APNs Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.push.service.apns.useDevelopment` | `false` | Flag indicating that the development instance of APNS service should be used. |
| `powerauth.push.service.apns.proxy.enabled` | `false` | Flag indicating if the communication needs to go through proxy. |
| `powerauth.push.service.apns.proxy.host` | `127.0.0.1` | Proxy host |
| `powerauth.push.service.apns.proxy.port` | `8080` | Proxy port |
| `powerauth.push.service.apns.proxy.username` | `_empty_` | Proxy username | 
| `powerauth.push.service.apns.proxy.password` | `_empty_` | Proxy password |
| `powerauth.push.service.apns.connect.timeout` | `5000` | Push message gateway connect timeout in milliseconds |
| `powerauth.push.service.apns.concurrentConnections` | `1` | Push message concurrency settings |

# FCM Configuration

| Property | Default | Note |
|---|---|---|
| `powerauth.push.service.fcm.proxy.enabled` | `false` | Flag indicating if the communication needs to go through proxy. |
| `powerauth.push.service.fcm.proxy.host` | `127.0.0.1` | Proxy host |
| `powerauth.push.service.fcm.proxy.port` | `8080` | Proxy port |
| `powerauth.push.service.fcm.proxy.username` | `_empty_` | Proxy username | 
| `powerauth.push.service.fcm.proxy.password` | `_empty_` | Proxy password |
| `powerauth.push.service.fcm.dataNotificationOnly` | `false` | Flag indicating that FCM service should never use "notification" format, only a data format with extra payload representing the notification. |
| `powerauth.push.service.fcm.sendMessageUrl` | `https://fcm.googleapis.com/v1/projects/%s/messages:send` | Default URL for the FCM service. |
| `powerauth.push.service.fcm.connect.timeout` | `5000` | Push message gateway connect timeout in milliseconds | 

# Deploying Push Server

Push Server is a Java EE application (packaged as an executable WAR file) that can be used to send push notifications to iOS or Android devices. This chapter explains what steps need to be taken in order to deploy PowerAuth Push Server.

## Downloading Push Server

You can download the latest `powerauth-push-server.war` at the releases page:

- [Push Server Releases](https://github.com/wultra/powerauth-push-server/releases)

## Database

### Setting Up Database Tables

The PowerAuth Push Server requires several new tables to be set up - refer to the separate documentation for the detailed description of these tables:

- [PowerAuth Push Server Database Structure](./Push-Server-Database.md)

The new tables may or may not reside in the same database that you use for your PowerAuth deployment.

### Connecting Server to Database

The default database connectivity parameters in `powerauth-push-server.war` are following (MySQL defaults):

```sh
spring.datasource.url=jdbc:mysql://localhost:3306/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.hibernate.ddl-auto=none
```

These parameters are of course only for the testing purposes, they are not suitable for production environment. They should be overridden for your production environment using a standard [Spring database connectivity related properties](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-sql.html#boot-features-connect-to-production-database).

As you can see, these credentials are the same as for the PowerAuth Server. You may use the same database for both applications but it is not required - Push Server can have own database.

## Configuration

The default implementation of a PowerAuth Push Server has only one compulsory configuration parameter `powerauth.service.url` that configures the SOAP endpoint location of a PowerAuth Server. The default value for this property points to `localhost`:

```bash
powerauth.service.url=http://localhost:8080/powerauth-java-server/soap
```

There are several optional configuration options you may want to set up.

### Configuration of Push service URL

Push server contains REST API which needs to be configured in case Push Server runs on non-standard port, non-standard context path or uses HTTPS. You can configure the service URL using following property:

```
powerauth.push.service.url=http://localhost:8080/powerauth-push-server
```

### Enabling Storing of Sent Push Messages

You can enable storing of sent messages in database using following property:

```
powerauth.push.service.message.storage.enabled=true
```

### Enabling Multiple Associated Activations in Device Registration 

You can enable registration of multiple associated activations for a push token using following property:

```
powerauth.push.service.registration.multipleActivations.enabled=true
```

### APNS Environment Configuration

In order to separate development and production environment on APNS, you may want to set following property:

```
powerauth.push.service.apns.useDevelopment=false
```

### Data-Only Notifications for FCM

In case you prefer only data notifications for the FCM service, you may want to enable following flag:

```sh
powerauth.push.service.fcm.dataNotificationOnly=true
```

If this flag is set to `true`, push server will use a key in `data` payload for title and subtitle, instead of placing them in the `notification` payload, like so:

```json
{
    "to": "asdfgh...fcm....token",
    "data": {
        "_notification": {
            "title": "Hello world",
            "subtitle": "Oh, my wife..."
        }
    }
}
```

### Running Behind Proxy

In order to run PowerAuth Push server behind the proxy, you simply need to configure additional properties. See [Running Behind Proxy](./Running-Behind-Proxy.md) chapter for details.

### Disabling SSL Validation During Development

_(optional)_ While this is **strongly discouraged in production environment** (we cannot emphasize this enough), some development environments may use self-signed certificate for HTTPS communication. In case PowerAuth SOAP service uses HTTPS with such certificate, and in case you are not able to correctly configure a custom keystore in your server container, you may disable SSL certificate validation by setting this property:

```bash
powerauth.service.ssl.acceptInvalidSslCertificate=true
```

### Setting Up Credentials

_(optional)_ In case PowerAuth Server uses a [restricted access flag in the server configuration](https://github.com/wultra/powerauth-server/blob/develop/docs/Deploying-PowerAuth-Server.md#enabling-powerauth-server-security), you need to configure credentials for the PowerAuth Push Server so that it can connect to the SOAP service:

```sh
powerauth.service.security.clientToken=
powerauth.service.security.clientSecret=
```

The credentials are stored in the `pa_integration` table.

_Note: For SOAP interface, PowerAuth Server uses WS-Security, `UsernameToken` validation (plain text password). The RESTful interface is secured using Basic HTTP Authentication (pre-emptive)._

## Using up ALPN

PowerAuth Push Server uses [Pushy](https://github.com/relayrides/pushy) to send notifications. Since Pushy uses the new HTTP/2 interface for sending APNs messages, underlying server must support this protocol. As a result, Java runtime / application container must support HTTP/2 as well.

### APNL and Tomcat 8.0

Put `alpn-boot` library (available [here](https://mvnrepository.com/artifact/org.mortbay.jetty.alpn/alpn-boot)) in `${CATALINA_HOME}/lib` folder and make sure to start Tomcat with `-Xbootclasspath/p:${CATALINA_HOME}/lib/alpn-boot.jar` parameters, so that the library is on classpath.

## Deploying Push Server

### Inside the Container

You can deploy PowerAuth Push Server into any Java EE container.

The default configuration works best with Apache Tomcat server running on default port 8080. In this case, the deployed server is accessible on `http://localhost:8080/powerauth-push-server/`.

To deploy PowerAuth Push Server to Apache Tomcat, simply copy the WAR file in your `webapps` folder or deploy it using the "Tomcat Web Application Manager" application (usually deployed on default Tomcat address `http://localhost:8080/manager`).

*__Important note: Since PowerAuth Push Server is a very simple application with direct access to the PowerAuth Server SOAP services, it must not be under any circumstances published publicly and must be constrained to the in-house closed infrastructure. The only exception to this rule is the requirement to open up ports for the purpose of communication with APNs and FCM services - the push notifications apparently would not work without access to the primary push service providers.__*

## Outside the Container

You can also execute WAR file directly using the following command:

```bash
java -jar powerauth-push-server.war
```

_Note: You can overwrite the port using `-Dserver.port=8090` parameter to avoid port conflicts._

*__Important note: Since PowerAuth Push Server is a very simple application with direct access to the PowerAuth Server SOAP services, it must not be under any circumstances published publicly and must be constrained to the in-house closed infrastructure. The only exception to this rule is the requirement to open up ports for the purpose of communication with APNs and FCM services - the push notifications apparently would not work without access to the primary push service providers.__*

## Deploying Push Server On JBoss / Wildfly

Follow the extra instructions in chapter [Deploying Push Server on JBoss / Wildfly](./Deploying-Wildfly.md).

# Running Behind Proxy

In case you would like to run push server behind a proxy, you need to configure following properties, otherwise service clients will not be able to resolve DNS records for APNS / FCM services:

```bash
# APNs Configuration
powerauth.push.service.apns.proxy.enabled=false
powerauth.push.service.apns.proxy.host=127.0.0.1
powerauth.push.service.apns.proxy.port=8080
powerauth.push.service.apns.proxy.username=
powerauth.push.service.apns.proxy.password=

# FCM Configuration
powerauth.push.service.fcm.proxy.enabled=false
powerauth.push.service.fcm.proxy.host=127.0.0.1
powerauth.push.service.fcm.proxy.port=8080
powerauth.push.service.fcm.proxy.username=
powerauth.push.service.fcm.proxy.password=
```

In case the `*.username` value is empty, authentication is not used. Otherwise, username and password is used for the authentication with proxy.

Of course, you need to set the properties according to your deployment model. For example, if you are running push server in Tomcat, add these properties in `powerauth-push-server.xml` context file placed in `${CATALINA_HOME}/conf/Catalina/localhost` folder, like so:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>

    <!-- ... other configurations ... -->

    <!-- APNS -->
    <Parameter name="powerauth.push.service.apns.proxy.enabled" value="true"/>
    <Parameter name="powerauth.push.service.apns.proxy.host" value="10.64.0.99"/>
    <Parameter name="powerauth.push.service.apns.proxy.port" value="8088"/>

    <!-- FCM -->
    <Parameter name="powerauth.push.service.fcm.proxy.enabled" value="true"/>
    <Parameter name="powerauth.push.service.fcm.proxy.host" value="10.64.0.99"/>
    <Parameter name="powerauth.push.service.fcm.proxy.port" value="8088"/>

</Context>
```

# Integration with Push Server

In order to register devices and send push notifications, the Push Server needs to be integrated with server applications. The first application that must communicate with the Push Server is a mobile API application (the one that publishes service specific API for mobile application).

## Prerequisites for the tutorial

- Running PowerAuth Server with available REST interface.
- Running PowerAuth Push Server with available REST interface.
- Knowledge of applications based on Spring Framework.
- Software: IDE, Application Server (Tomcat, Wildfly, ...)

## Common integration steps

### Adding Maven Dependency

In order to be able to implement integration easily, add Push Server client Maven dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>io.getlime.security</groupId>
    <artifactId>powerauth-push-client</artifactId>
    <version>${powerauth.push-server.version}</version>
</dependency>
```

### Prepare a Service Client Configuration

In order to connect to the PowerAuth Push Server, you need to add following configuration:

```java
@Configuration
@ComponentScan(basePackages = {"io.getlime.push"})
public class PowerAuthPushConfiguration {

  @Bean
  public PushServerClient pushServerClient() {
    PushServerClient client = new PushServerClient();
    client.setServiceBaseUrl("http://localhost:8080/powerauth-push-server");
    return client;
  }

}
```

## Integration With Mobile API

### Prepare the Device Registration Endpoint

In order to implement generic device registration, implement a custom registration RESTful Endpoint that calls the Push Server under the hood, for example like so:

```java
@Controller
@RequestMapping(value = "push")
public class DeviceRegistrationController {

    @Autowired
    private PushServerClient pushServerClient;

    private static final Long APP_ID = 1; // Replace by your app ID or use a configuration class

    @RequestMapping(value="device/create", method = RequestMethod.POST)
    public @ResponseBody String registerDevice(@RequestBody Map<String,String> request) {

        // Get the values from the request
        String platform = request.get("platform");
        String token = request.get("push_token");

        // Check if the context is authenticated - if it is, add activation ID.
        // This assures that the activation is assigned with a correct device.
        String activationId = null;
        PowerAuthApiAuthentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            activationId = authentication.getActivationId();
        }

        // Register the device and return response
        boolean result = pushServerClient.registerDevice(APP_ID, token, MobilePlatform.valueOf(platform), activationId);
        if (result) {
            return "OK";
        } else {
            return "NOT_OK";
        }
    }
}
```

The code checks for `PowerAuthApiAuthentication` instance in the security context and if it is present, it binds the activation ID with the push service token.

**Note: We are using 'dummy' request (Map<String,String>) and response (String) objects. Replace them with your proprietary classes.**

## Integrate with Push Producer Applications

Push Producer Application is any application that sends push notification request. The purpose of the producer applications is to call service for push notification sending (single or batch):

```java
// Prepare push message object
PushMessage push = new PushMessage();
push.setUserId("123");

// Set push message attributes
push.getAttributes().setSilent(false);
push.getAttributes().setPersonal(true);

// Set push message body
PushMessageBody body = new PushMessageBody();
body.setTitle("Balance update");
body.setBody("Your balance is now $745.00");
body.setSound("default");
body.setBadge(1);
body.setCategory("balance");
body.setCollapseKey("balance");
body.setValidUntil(ISODate("2016-10-12T11:20:04Z").date());

// Add custom message data attributes
Map<String,Object> extras = new HashMap<>();
extras.put("customKey", "customValue");
extras.put("customDate", new Date());
body.setExtras(extras);

// Set the new push message body
push.setBody(body);

// Send single push message
pushServerClient.sendNotification(APP_ID, push);

// Send push message batch
List<PushMessage> messageList = new ArrayList<>();
messageList.add(push);
pushServerClient.sendNotificationBatch(APP_ID, messageList);
```

## Other Tasks

### Disabling SSL Certificate Validation

In testing or development environments, you may disable SSL validation by setting an attribute when creating `PushServerClient` bean. Make sure that this setting is not used in production environment.

```java
@Bean
public PushServerClient pushServerClient() {
    PushServerClient client = new PushServerClient();
    client.setServiceBaseUrl(powerAuthPushServiceUrl);
    // whether invalid SSL certificates should be accepted
    if (acceptInvalidSslCertificate) {
        sslConfigurationService.trustAllCertificates();
    }
    return client;
}
```

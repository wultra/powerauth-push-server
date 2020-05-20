# Migration from 0.23.0 to 0.24.0

## Unirest Initialization

In previous versions, we included configuration of [Unirest](https://kong.github.io/unirest-java/) client right in the Push Server client code. This was incorrect, since Unirest initializes in a static manner. Our configuration could be clashing with other components using Unirest. As a result, you need to add Unirest configuration yourself soon after the application launch in case you would like to use our client library. In the case you call our API's yourself, no changes are needed.

Below is a minimal Unirest configuration plugged into the Spring framework in a way to reuse `ObjectMapper` configuration. Of course, you can use any other [Unirest configuration parameters](https://kong.github.io/unirest-java/#configuration).

```java
@Configuration
public class UnirestConfiguration {

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper mapper;

    @PostConstruct
    public void postConstruct() {
        Unirest.config().setObjectMapper(new ObjectMapper() {

            public String writeValue(Object value) {
                try {
                    return mapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return mapper.readValue(value, valueType);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
```

## Push Server Administration  

In the latest version of Push Server, we decided to remove the web administration console, due to its simplicity. You can configure apps either via database by inserting values to the `push_app_credentials` table, or by calling [Administration API](./Push-Server-API.md#administration). A new section regarding Push Server administration is [available in the documentation](./Push-Server-Administration.md). 

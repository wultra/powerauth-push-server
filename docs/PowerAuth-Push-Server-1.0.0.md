# Migration from 0.24.0 to 1.0.0

## Push Client Migrated to WebClient

Push client now uses WebClient which is an HTTP client based on Spring WebFlux. We made this
change to unify HTTP clients across the whole PowerAuth stack.

In case you use the provided Push client, the Unirest configuration is no longer required, so you can safely 
remove any Unirest configuration from your project.

## Migration of PowerAuth client to REST interface

PowerAuth client uses REST interface in version `1.0.0`. Previous versions of Push server used the SOAP interface. This change needs to be reflected in configuration property `powerauth.service.url`.

Property value before migration:
`powerauth.service.url=http://[server]:[port]/powerauth-java-server/soap`

Property value after migration:
`powerauth.service.url=http://[server]:[port]/powerauth-java-server/rest`
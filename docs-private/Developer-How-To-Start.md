# Developer - How to Start Guide


## General Prerequisites

Following are _minimal_ versions of the tools and technologies used in the development
of the PowerAuth Push Server. You can use higher versions, but make sure to check the compatibility.

* _JDK_ version 21.x
* _Maven_ version 3.9.x
* _PostgreSQL_ version 18.x
* _Liquibase_ version 4.33.x


## Push Server


### Build

From the repository root, build all modules with:

```shell
mvn clean install
```

To build only the Push Server module and its dependencies from the repository root, use:

```shell
mvn -pl powerauth-push-server -am clean install
```


### Database

* The default DB for development is _PostgreSQL_.
* Database changes are driven by Liquibase.


#### Set up

Ensure you have a database installed and running, and that you have an admin account.


##### Create a user and a database

Start a `psql` session with your superuser:

```shell
psql -U $(whoami) -d postgres
```

Then run following commands in the `psql` shell:

```sql
CREATE USER powerauth;
CREATE DATABASE powerauth OWNER powerauth;
```

By default, local development in this repository uses the `powerauth` user without a password.
If your local PostgreSQL setup requires password authentication, set a password for the user
and update the matching datasource and Liquibase settings in the commands below.


##### Load the data with Liquibase

The `dev` Spring profile (see `application-dev.properties`) enables Liquibase auto-migration,
so the database schema is applied automatically on application startup. No manual Liquibase
commands are needed for regular development.

To manually check the Liquibase status, you can run:

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth status
```

To manually apply the changesets:

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth update
```


### Configure

For local development, the provided IntelliJ IDEA run configuration uses the `dev`
Spring profile together with `powerauth-push-server/src/main/resources/application-dev.properties`.

The `dev` profile enables:

* Liquibase auto-migration (`spring.liquibase.enabled=true`)
* Multiple activations per device (`powerauth.push.service.registration.multipleActivations.enabled=true`)
* Shorter cache refresh (`powerauth.push.service.clients.cache.refreshAfterWrite=1m`)
* Debug logging for Wultra packages (`logging.level.com.wultra=DEBUG`)

Outside this local `dev` setup, the default application configuration enables the `ext`
profile, so you can override values using `application-ext.properties` or environment
variables.

Common properties to review for local development (see `application.properties`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/powerauth
spring.datasource.username=powerauth
spring.datasource.password=
powerauth.service.url=http://localhost:8080/powerauth-java-server/rest
```

For additional details, see:

* [Configuration Properties](../docs/Configuration-Properties.md)
* [Deploying Push Server](../docs/Deploying-Push-Server.md)


### Run

The working directory is `powerauth-push-server`.


#### CLI

```shell
java -jar target/powerauth-push-server-x.y.z.war --spring.profiles.active=dev
```

The exact WAR filename can be found in the `target/` directory.


#### Maven

```shell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```


#### IntelliJ IDEA

* Use IntelliJ IDEA run configuration at `../.run/PowerAuthPushServerJavaApplication.run.xml`
* The provided run configuration starts the server on `http://localhost:8089/powerauth-push-server`


### Smoke test

If you use the provided IntelliJ IDEA run configuration, run:

```shell
curl -v http://localhost:8089/powerauth-push-server/actuator/health
```

If you run the server directly from CLI or Maven without extra server parameters, run:

```shell
curl -v http://localhost:8080/actuator/health
```

You should get response: `200 {"status":"UP"}`

You can check other APIs on:

* http://localhost:8089/powerauth-push-server/swagger-ui/index.html


### Generate SQL script (optional)


#### PostgreSQL

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --output-file=./docs/sql/postgresql/generated-postgresql-script.sql updateSQL --url=offline:postgresql
```


#### Oracle

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --output-file=./docs/sql/oracle/generated-oracle-script.sql updateSQL --url=offline:oracle
```


#### MS SQL

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --output-file=./docs/sql/mssql/generated-mssql-script.sql updateSQL --url=offline:mssql
```

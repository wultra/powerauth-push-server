# Developer - How to Start Guide


## Push Server


### Standalone Run

- Use IntelliJ Idea run configuration at `../.run/PowerAuthPushServerJavaApplication.run.xml`
- Open [http://localhost:8089/powerauth-push-server/actuator/health](http://localhost:8089/powerauth-push-server/actuator/health) and you should get `{"status":"UP"}`


### Database

Database changes are driven by Liquibase.

This is an example how to manually check the Liquibase status.
Important and fixed parameter is `changelog-file`.
Others (like URL, username, password) depend on your environment.

```shell
liquibase --changelog-file=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --url=jdbc:postgresql://localhost:5432/powerauth --username=powerauth status
``` 

To generate SQL script run following command:

#### PostgreSQL

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --output-file=./docs/sql/postgresql/update.sql updateSQL --url=offline:postgresql
```

#### Oracle

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --output-file=./docs/sql/oracle/update.sql updateSQL --url=offline:oracle
```


#### MSSQL

```shell
liquibase --changeLogFile=./docs/db/changelog/changesets/powerauth-push-server/db.changelog-module.xml --output-file=./docs/sql/mssql/update.sql updateSQL --url=offline:mssql
```

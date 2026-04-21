FROM liquibase:4.33.0-alpine

LABEL maintainer="Wultra <hello@wultra.com>"

COPY docs/db/changelog/changesets/powerauth-push-server /liquibase/
COPY powerauth-push-server/docker-init/docker-entrypoint.sh /docker-entrypoint.sh

RUN rm /liquibase/internal/lib/snowflake-jdbc.jar
RUN rm /liquibase/internal/lib/mssql-jdbc.jar

USER liquibase
ENTRYPOINT ["/docker-entrypoint.sh"]

FROM ibm-semeru-runtimes:open-21.0.8_9-jre

LABEL maintainer="Wultra <hello@wultra.com>"

# Prepare environment variables
ENV JAVA_HOME=/opt/java/openjdk \
    PKG_RELEASE=1~jammy \
    LOGBACK_CONF=/opt/logback/conf \
    TZ=UTC \
    APP_PATH="/app/powerauth-push-server.war" \
    EXTLIB_PATH="/app/extlib" \
    OJDBC_VERSION="23.8.0.25.04"

# Upgrade OS and dependencies
RUN apt-get -y update  \
    && apt-get -y upgrade \
    && apt-get -y install bash wget \
    && mkdir -p ${EXTLIB_PATH} && wget --no-verbose https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc11/${OJDBC_VERSION}/ojdbc11-${OJDBC_VERSION}.jar -O ${EXTLIB_PATH}/ojdbc11.jar \
# Uninstall packages which are no longer needed and clean apt caches
    && apt-get -y remove wget gettext-base \
    && apt-get -y purge --auto-remove \
    && rm -rf /tmp/* /var/cache/apt/* \
# Add PowerAuth user
    && groupadd -r powerauth && useradd -r -g powerauth -s /sbin/nologin powerauth

# Deploy and run applications
COPY ./powerauth-push-server/docker/images-and-libs/powerauth-push-server.war "${APP_PATH}"

# Docker configuration
EXPOSE 8080
STOPSIGNAL SIGQUIT

# Add PowerAuth User
USER powerauth

COPY powerauth-push-server/docker/conf/logback/* $LOGBACK_CONF/

# Define entry point with mandatory commands
COPY powerauth-push-server/docker/docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
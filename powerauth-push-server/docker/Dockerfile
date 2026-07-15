FROM ghcr.io/wultra/wultra-base-images:jre-semeru-25_2

# Prepare environment variables
ENV LOGBACK_CONF=/opt/logback/conf \
    APP_PATH="/app/powerauth-push-server.war" \
    CONTEXT_PATH="/powerauth-push-server"

# Deploy and run applications
COPY ./powerauth-push-server/target/powerauth-push-server*.war "${APP_PATH}"

COPY powerauth-push-server/docker/conf/logback/* $LOGBACK_CONF/
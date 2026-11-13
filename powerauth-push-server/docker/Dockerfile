FROM ghcr.io/wultra/wultra-base-images:jre-semeru-25_7@sha256:30d496462a9fa1ead309d9397f23a27f6deadf235733547e7c24a4040a3f98fa

# Prepare environment variables
ENV LOGBACK_CONF=/opt/logback/conf \
    APP_PATH="/app/powerauth-push-server.war" \
    CONTEXT_PATH="/powerauth-push-server"

# Deploy and run applications
COPY ./powerauth-push-server/target/powerauth-push-server*.war "${APP_PATH}"

COPY powerauth-push-server/docker/conf/logback/* $LOGBACK_CONF/
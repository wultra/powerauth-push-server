#!/usr/bin/env bash
set -euo pipefail

exec java -Dserver.port=8080 -Dspring.config.additional-location=/app/application.properties ${JAVA_OPTS:-} -Dserver.servlet.context-path=/powerauth-push-server -cp "${APP_PATH}:${EXTLIB_PATH}/*" org.springframework.boot.loader.launch.WarLauncher
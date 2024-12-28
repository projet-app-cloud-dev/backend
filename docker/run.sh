#!/usr/bin/env sh
set -x
if [[ -z "${APP_INSIGHTS}" ]] || [[ -z "${APP_NAME}" ]]; then
  echo "Missing APP_INSIGHTS OR APP_NAME, running without app insights"
  java -noverify -Dspring.aot.enabled=true -jar /app/spring-boot-application.jar --server.port=8080
else
  json="{\"connectionString\": \"$APP_INSIGHTS\", \"role\": {\"name\": \"$APP_NAME\"}}"
  env APPLICATIONINSIGHTS_CONFIGURATION_CONTENT="$json" java -noverify -javaagent:"applicationinsights-agent-3.6.2.jar" -Dspring.aot.enabled=true -jar /app/spring-boot-application.jar --server.port=8080
fi

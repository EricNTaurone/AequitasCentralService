#!/usr/bin/env sh
set -euo pipefail

exec java ${JAVA_OPTS:-} -jar /app/app.jar

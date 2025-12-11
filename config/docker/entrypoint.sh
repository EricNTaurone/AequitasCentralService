#!/bin/bash
set -euo pipefail

# Enable JVM debug if JAVA_DEBUG_OPTS is set (typically in dev environment)
exec java ${JAVA_OPTS:-} ${JAVA_DEBUG_OPTS:-} -jar /app/app.jar

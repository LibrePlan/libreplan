#!/bin/bash
set -uo pipefail
ROOT=/home/jeroen/src/libreplan-vibe-cli/claude/libreplan
LOG="$ROOT/jetty-current.log"

cd "$ROOT"
mvn -pl libreplan-webapp -P-reports,-userguide,-i18n -DskipTests package -q 2>&1 | tail -40
if [ ${PIPESTATUS[0]} -ne 0 ]; then
  echo "BUILD FAILED"
  exit 1
fi

pid=$(ss -tlnp 2>/dev/null | grep 8080 | grep -oP 'pid=\K[0-9]+')
if [ -n "${pid:-}" ]; then
  kill "$pid"
  sleep 2
fi

cd "$ROOT/libreplan-webapp"
export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx512m"
nohup mvn -Djetty.port=8099 jetty:run-war > "$LOG" 2>&1 &
newpid=$!
echo "started pid $newpid"

for i in $(seq 1 40); do
  code=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8080/ 2>/dev/null)
  if [ "$code" != "000" ]; then
    echo "UP code=$code after ~$((i*3))s"
    exit 0
  fi
  sleep 3
done
echo "TIMED OUT waiting for server"
tail -40 "$LOG"
exit 1

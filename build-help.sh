#!/bin/bash
#set -euo pipefail

WARFILE="libreplan-webapp/target/libreplan-webapp.war"

if [[ -x "./mvnw" ]]; then
  MVN_ROOT="./mvnw"
  MVN_WEBAPP="../mvnw"
else
  MVN_ROOT="mvn"
  MVN_WEBAPP="mvn"
fi

rm -f "$WARFILE"
rm -rf "libreplan-webapp/target/classes/org/libreplan/web/"

psql -c 'drop table databasechangeloglock; ' libreplandev

"$MVN_ROOT" -P-reports,-i18n -DskipTests clean install | tee lp-build.log

if [[ -f "$WARFILE" ]]; then
  # read -p "Press Enter to start web interface" dummy
  cd libreplan-webapp
  export MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx512m"
  "$MVN_WEBAPP" -Djetty-port=8099 jetty:run-war | tee -a lp-build.log
fi


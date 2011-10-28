#!/bin/bash

LIBREPLAN_WEBAPP="../libreplan-webapp";
LIBREPLAN_GANTTZK="../ganttzk";
LIBREPLAN_BUSINESS="../libreplan-business";

WEBAPP_KEYS="$LIBREPLAN_WEBAPP/src/main/resources/i18n/keys.pot"
GANTTZK_KEYS="$LIBREPLAN_GANTTZK/src/main/resources/i18n/keys.pot"

# Parse webapp java and zul and ganttzk validation messages
WKDIR=`pwd`
cd $LIBREPLAN_WEBAPP
mvn gettext:gettext 2> /dev/null
cd "${WKDIR}"

if [ ! -f $WEBAPP_KEYS ]
    then touch $WEBAPP_KEYS
fi
./gettext-keys-generator.pl -d $LIBREPLAN_WEBAPP -k $WEBAPP_KEYS 2> /dev/null
./gettext-keys-generator.pl --java -d $LIBREPLAN_BUSINESS -k $WEBAPP_KEYS 2> /dev/null
find $LIBREPLAN_BUSINESS/src -name "*.java" -exec xgettext -j --from-code=utf-8 -k_ -o $WEBAPP_KEYS '{}' \;

# Parse ganttzk java and zul
cd $LIBREPLAN_GANTTZK
mvn gettext:gettext 2> /dev/null
cd "${WKDIR}"

if [ ! -f $GANTTZK_KEYS ]
    then touch $GANTTZK_KEYS
fi
./gettext-keys-generator.pl -d $LIBREPLAN_GANTTZK -k $GANTTZK_KEYS 2> /dev/null

# Convert absolute paths to relative
sed -i 's/\(#: \)\(.*\)\/\(ganttzk\|libreplan-business\|libreplan-webapp\)\/\(.*\)/\1\3\/\4/' $WEBAPP_KEYS
sed -i 's/\(#: \)\(.*\)\/\(ganttzk\|libreplan-business\|libreplan-webapp\)\/\(.*\)/\1\3\/\4/' $GANTTZK_KEYS

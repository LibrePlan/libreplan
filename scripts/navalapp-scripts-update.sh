#!/bin/sh
set -e

BASE_URL=http://naval.igalia.com/downloads

wget $BASE_URL/navalapp-application-update.txt
wget $BASE_URL/navalapp-database-update.txt

mv navalapp-application-update.txt navalapp-application-update.sh
mv navalapp-database-update.txt navalapp-database-update.sh

chmod +x navalapp-application-update.sh navalapp-database-update.sh

echo Se han actualizado los scripts de actualización pulse una tecla para terminar

read foo


#!/bin/sh
set -e

CONFIG_FILENAME=navalapp-download-config.txt
UPDATE_CONFIG_URL=http://naval.igalia.com/downloads/$CONFIG_FILENAME

wget $UPDATE_CONFIG_URL
. ./$CONFIG_FILENAME
rm -f $CONFIG_FILENAME

HOME_NAVALAPP=/var/lib/tomcat6

cd $HOME_NAVALAPP/webapps
sudo rm -f $NAVAL_APP_WAR_FILENAME
sudo wget  $NAVAL_APP_WAR_URL
sudo chown tomcat6.tomcat6 $NAVAL_APP_WAR_FILENAME
sudo /etc/init.d/tomcat6 restart
cd -


echo Se han actualizado la aplicacion puede consultarla en http://localhost:8080/navalplanner-webapp/
echo Pulse una tecla para terminar

read foo

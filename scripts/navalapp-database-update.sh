#!/bin/sh
set -e

CONFIG_FILENAME=navalapp-download-config.txt
UPDATE_CONFIG_URL=http://naval.igalia.com/downloads/$CONFIG_FILENAME

wget $UPDATE_CONFIG_URL
. ./$CONFIG_FILENAME
rm -f $CONFIG_FILENAME

DUMP_FILENAME_UNCOMPRESSED=`echo $NAVAL_DATABASE_DUMP_FILENAME|sed "s/\.bz2$//g"`

cd /tmp
sudo su -c "rm -f /tmp/$NAVAL_DATABASE_DUMP_FILENAME"
sudo su -c "rm -f /tmp/$DUMP_FILENAME_UNCOMPRESSED"
wget $NAVAL_DATABASE_DUMP_URL
bunzip2 $NAVAL_DATABASE_DUMP_FILENAME

sudo su -c "psql navaldev < $DUMP_FILENAME_UNCOMPRESSED" postgres
cd -


echo Se han actualizado la base de datos $DUMP_FILENAME_UNCOMPRESSED con la versión inicial de datos de prueba
echo Pulse una tecla para terminar

read foo

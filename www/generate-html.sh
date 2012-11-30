#!/bin/bash

FILES="\
    AUTHORS.rst \
    HACKING.rst \
    INSTALL.rst \
    NEWS.rst \
    README.rst \
    README.Fedora.rst \
    README.openSUSE.rst \
    TODO.rst \
    UPDATE.rst \
    howto-create-a-new-report-in-libreplan.rst \
    howto-develop-a-use-case-in-libreplan.rst \
    howto-start-development-with-eclipse.rst \
    libreplan-web-services.rst \
    "

TMP=`mktemp`

for file in $FILES
do
    output=${file%\.rst}.html

    # Backup file
    if [ -s $output ]
    then
        mv $output $output.bak
    fi

    # Generate HTML
    rst2html --link-stylesheet --stylesheet-path=lsr.css $file $output > /dev/null 2>$TMP

    # Check errors output
    if [ -s $TMP ]
    then
        # Back to original file if errors
        echo "Parsing errors in file \"$file\" keeping old file"
        if [ -s $output.bak ]
        then
            mv $output.bak $output
        fi
        rm $TMP
    else
        # Remove backup
        if [ -s $output.bak ]
        then
            rm $output.bak
        fi
    fi
done

rm $TMP

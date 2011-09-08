#!/bin/bash
if [ $# -ne 3 ]
then
echo "Usage: testrunner.sh <sah file|suite file> <startURL> <browserType>"
echo "File path is relative to <libreplan root folder>/scripts/functional-tests"
echo "Example:"
echo "./testrunner.sh data-types/all_data_type_test.suite http://localhost:8080/navalplanner-webapp <browserType>"
echo "./testrunner.sh data-types/progress_test.sah http://localhost:8080/navalplanner-webapp <browserType>"
else

. ./sahi-common-env.sh

export USERDATA_DIR=$SAHI_HOME/userdata/
export SCRIPTS_PATH=`pwd`/$1
export BROWSER=$3
export START_URL=$2
export THREADS=1
export SINGLE_SESSION=false
java -cp $SAHI_HOME/lib/ant-sahi.jar net.sf.sahi.test.TestRunner -test $SCRIPTS_PATH -browserType "$BROWSER" -baseURL $START_URL -host localhost -port 9999 -threads $THREADS -useSingleSession $SINGLE_SESSION
fi

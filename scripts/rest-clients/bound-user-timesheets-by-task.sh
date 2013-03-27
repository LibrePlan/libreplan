#!/bin/sh

. ./rest-common-env.sh

printf "BOUND USER\n"
printf "Username: "
read loginName
printf "Password: "
read password

task=$1

if [ "$1" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
    task=$2
elif [ "$1" = "--dev" ]; then
    baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
    certificate=$DEVELOPMENT_CERTIFICATE
    task=$2
else
    baseServiceURL=$DEMO_BASE_SERVICE_URL
    certificate=$DEMO_CERTIFICATE
fi

if [ "$task" = "" ]; then
    printf "Missing task\n" 1>&2
    exit 1
fi

authorization=`echo -n "$loginName:$password" | base64`

result=`curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
    $baseServiceURL/bounduser/timesheets/$task`

if hash tidy &> /dev/null; then
    echo $result | tidy -xml -i -q -utf8
else
    echo $result
fi

#!/bin/sh

. ./rest-common-env.sh

printf "BOUND USER\n"
printf "Username: "
read loginName
printf "Password: "
read password

if [ "$1" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
elif [ "$1" = "--dev" ]; then
    baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
    certificate=$DEVELOPMENT_CERTIFICATE
else
    baseServiceURL=$DEMO_BASE_SERVICE_URL
    certificate=$DEMO_CERTIFICATE
fi

authorization=`echo -n "$loginName:$password" | base64`

result=`curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
    $baseServiceURL/bounduser/mytasks`

if hash tidy &> /dev/null; then
    echo $result | tidy -xml -i -q -utf8
else
    echo $result
fi

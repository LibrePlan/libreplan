#!/bin/sh

. ./rest-common-env.sh

printf "Username: "
read loginName
printf "Password: "
read password

code=$2

if [ "$2" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
    code=$3
elif [ "$2" = "--dev" ]; then
    baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
    certificate=$DEVELOPMENT_CERTIFICATE
    code=$3
else
    baseServiceURL=$DEMO_BASE_SERVICE_URL
    certificate=$DEMO_CERTIFICATE
fi

authorization=`echo -n "$loginName:$password" | base64`

result=`curl -sv -X DELETE $certificate --header "Authorization: Basic $authorization" \
    $baseServiceURL/$1/$code`

if hash tidy &> /dev/null; then
    echo $result | tidy -xml -i -q -utf8
else
    echo $result
fi

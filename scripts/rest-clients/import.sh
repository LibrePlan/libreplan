#!/bin/sh

. ./rest-common-env.sh

printf "Username: "
read loginName
printf "Password: "
read password

file=$2

if [ "$2" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
    file=$3
elif [ "$2" = "--dev" ]; then
    baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
    certificate=$DEVELOPMENT_CERTIFICATE
    file=$3
else
    baseServiceURL=$DEMO_BASE_SERVICE_URL
    certificate=$DEMO_CERTIFICATE
fi

if [ "$file" = "" ]; then
    printf "Missing file\n" 1>&2
    exit 1
fi

authorization=`echo -n "$loginName:$password" | base64`

result=`curl -sv -X POST $certificate -d @$file \
    --header "Content-type: application/xml" \
    --header "Authorization: Basic $authorization" \
    $baseServiceURL/$1`

if hash tidy &> /dev/null; then
    echo $result | tidy -xml -i -q -utf8
else
    echo $result
fi

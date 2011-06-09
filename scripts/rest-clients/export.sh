#!/bin/sh

. ./rest-common-env.sh

printf "Login name: "
read loginName
printf "Password: "
read password

if [ "$2" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
elif [ "$2" = "--dev" ]; then
   baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
   certificate=$DEVELOPMENT_CERTIFICATE
else
   baseServiceURL=$DEMO_BASE_SERVICE_URL
   certificate=$DEMO_CERTIFICATE
fi

authorization=`./base64.sh $loginName:$password`

curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
    $baseServiceURL/$1 | tidy -xml -i -q -utf8

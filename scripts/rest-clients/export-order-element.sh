#!/bin/sh

. ./rest-common-env.sh

printf "Login name: "
read loginName
printf "Password: "
read password

if [ "$1" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
else
   baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
   certificate=$DEVELOPMENT_CERTIFICATE
fi

authorization=`./base64.sh $loginName:$password`

curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
    $baseServiceURL/orderelements/ | tidy -xml -i -q -utf8

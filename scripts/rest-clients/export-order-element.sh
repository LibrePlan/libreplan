#!/bin/sh

. ./rest-common-env.sh

printf "Login name: "
read loginName
printf "Password: "
read password

baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
certificate=$DEVELOPMENT_CERTIFICATE

for i in "$@"
do
    if [ "$i" = "--prod" ]; then
        baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
        certificate=$PRODUCTION_CERTIFICATE
    else
       orderElementCode=$i
    fi
done

if [ "$orderElementCode" = "" ]; then
    printf "Missing order element code\n" 1>&2
    exit 1
fi

authorization=`./base64.sh $loginName:$password`

curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
    $baseServiceURL/orderelements/$orderElementCode/ | tidy -xml -i -q -utf8

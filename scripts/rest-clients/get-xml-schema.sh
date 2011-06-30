#!/bin/sh

. ./rest-common-env.sh

printf "Login name: "
read loginName
printf "Password: "
read password

if [ "$1" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
    service=$2
elif [ "$1" = "--dev" ]; then
    baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
    certificate=$DEVELOPMENT_CERTIFICATE
    service=$2
else
    baseServiceURL=$DEMO_BASE_SERVICE_URL
    certificate=$DEMO_CERTIFICATE
    service=$1
fi

if [ "$service" = "" ]; then
    printf "Missing service path\n" 1>&2
    exit 1
fi

authorization=`echo -n "$loginName:$password" | base64`

wget --no-check-certificate \
   --header "Authorization: Basic $authorization" \
   -O $service-schema.xml \
   $baseServiceURL/$service/?_wadl&_type=xml

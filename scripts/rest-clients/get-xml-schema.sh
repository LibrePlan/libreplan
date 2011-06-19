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
       service=$i
    fi
done

if [ "$service" = "" ]; then
    printf "Missing service path\n" 1>&2
    exit 1
fi

authorization=`echo -n "$loginName:$password" | base64`

wget --no-check-certificate \
   --header "Authorization: Basic $authorization" \
   -O $service-schema.xml \
   $baseServiceURL/$service/?_wadl&_type=xml

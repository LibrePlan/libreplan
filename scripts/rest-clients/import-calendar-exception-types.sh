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
       file=$i
    fi
done

if [ "$file" = "" ]; then
    printf "Missing file\n" 1>&2
    exit 1
fi

authorization=`./base64.sh $loginName:$password`

curl -sv -X POST $certificate -d @$file \
    --header "Content-type: application/xml" \
    --header "Authorization: Basic $authorization" \
    $baseServiceURL/calendarexceptiontypes | tidy -xml -i -q -utf8

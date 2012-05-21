#!/bin/sh

. ./rest-common-env.sh

printf "Username: "
read loginName
printf "Password: "
read password

baseServiceURL=$DEMO_BASE_SERVICE_URL
certificate=$DEMO_CERTIFICATE

if [ "$1" = "--prod" ]; then
    baseServiceURL=$PRODUCTION_BASE_SERVICE_URL
    certificate=$PRODUCTION_CERTIFICATE
    if [ "$#" = 4 ]; then
        resourceCode=$2
        startDate=$3
        endDate=$4
    else
        startDate=$2
        endDate=$3
    fi
elif [ "$1" = "--dev" ]; then
   baseServiceURL=$DEVELOPMENT_BASE_SERVICE_URL
   certificate=$DEVELOPMENT_CERTIFICATE
    if [ "$#" = 4 ]; then
        resourceCode=$2
        startDate=$3
        endDate=$4
    else
        startDate=$2
        endDate=$3
    fi
else
    if [ "$#" = 3 ]; then
        resourceCode=$1
        startDate=$2
        endDate=$3
    else
        startDate=$1
        endDate=$2
    fi
fi

if [ "$startDate" = "" ]; then
    printf "Missing start date\n" 1>&2
    exit 1
fi

if [ "$endDate" = "" ]; then
    printf "Missing end date\n" 1>&2
    exit 1
fi

authorization=`echo -n "$loginName:$password" | base64`

if [ "$resourceCode" = "" ]; then
    serviceURIWithParams="$baseServiceURL/resourceshours/$startDate/$endDate/"
else
    serviceURIWithParams="$baseServiceURL/resourceshours/$resourceCode/$startDate/$endDate/"
fi

result=`curl -sv -X GET $certificate --header "Authorization: Basic $authorization" \
    $serviceURIWithParams`

if hash tidy &> /dev/null; then
    echo $result | tidy -xml -i -q -utf8
else
    echo $result
fi

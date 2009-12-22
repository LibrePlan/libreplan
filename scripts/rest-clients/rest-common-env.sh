DEVELOPMENT_BASE_SERVICE_URL=http://localhost:8080/navalplanner-webapp/ws/rest
PRODUCTION_BASE_SERVICE_URL=https://naval.igalia.com/navalplanner-webapp/ws/rest

DEVELOPMENT_CERTIFICATE=""
# FIXME: Until we have a valid production certificate, accept any
# server-provided certificate.
PRODUCTION_CERTIFICATE=-k
#PRODUCTION_CERTIFICATE=--cacert igalia-certificate.pem

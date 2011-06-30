DEMO_BASE_SERVICE_URL=http://demo.navalplan.org/navalplan-demo/ws/rest
DEVELOPMENT_BASE_SERVICE_URL=http://localhost:8080/navalplanner-webapp/ws/rest
PRODUCTION_BASE_SERVICE_URL=http://localhost:8080/navalplan/ws/rest

DEMO_CERTIFICATE=""
DEVELOPMENT_CERTIFICATE=""
PRODUCTION_CERTIFICATE=""
# FIXME: Until we have a valid production certificate, accept any
# server-provided certificate.
#PRODUCTION_CERTIFICATE=-k
#PRODUCTION_CERTIFICATE=--cacert igalia-certificate.pem

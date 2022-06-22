#!/bin/bash

set -eux;

# run trino gateway spring boot application.
java \
-cp trino-gateway-*.jar \
-Dloader.path=/opt/trino-gateway/ \
-Dspring.config.location=file:///opt/trino-gateway/conf/application.properties \
-Dspring.config.location=file:///opt/trino-gateway/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher
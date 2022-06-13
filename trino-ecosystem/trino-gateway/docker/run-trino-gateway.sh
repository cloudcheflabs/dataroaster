#!/bin/bash

set -eux;

java \
-cp trino-gateway-*.jar \
-Dloader.path=/opt/trino-gateway/ \
-Dspring.config.location=file:///opt/trino-gateway/conf/application.properties \
-Dspring.config.location=file:///opt/trino-gateway/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher
#!/bin/bash

set -eux;

# run trino controller spring boot application.
java \
-cp trino-controller-*.jar \
-Dloader.path=/opt/trino-controller/ \
-Dspring.config.location=file:///opt/trino-controller/conf/application.properties \
-Dspring.config.location=file:///opt/trino-controller/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher
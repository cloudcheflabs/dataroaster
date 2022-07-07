#!/bin/bash

set -eux;

# run trino operator spring boot application.
java \
-cp trino-operator-*.jar \
-Dloader.path=/opt/trino-operator/ \
-Dspring.config.location=file:///opt/trino-operator/conf/application.properties \
-Dspring.config.location=file:///opt/trino-operator/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher
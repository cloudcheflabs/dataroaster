#!/bin/bash

set -eux;

# run dataroaster operator spring boot application.
java \
-cp dataroaster-operator-*.jar \
-Dloader.path=/opt/dataroaster-operator/ \
-Dspring.config.location=file:///opt/dataroaster-operator/conf/application.properties \
-Dspring.config.location=file:///opt/dataroaster-operator/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher
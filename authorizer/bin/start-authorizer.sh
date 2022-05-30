#!/bin/bash

AUTHORIZER_HOME=$(pwd);

nohup java \
-cp authorizer-*.jar \
-Dloader.path=$AUTHORIZER_HOME/ \
-Dspring.config.location=file://$AUTHORIZER_HOME/conf/application.properties \
-Dspring.config.location=file://$AUTHORIZER_HOME/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher > /dev/null 2>&1 &

PID=$!
echo "$PID" > pid;
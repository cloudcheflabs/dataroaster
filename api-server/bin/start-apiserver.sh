#!/bin/bash

APISERVER_HOME=$(pwd);

nohup java \
-cp api-server-*.jar \
-Dloader.path=$APISERVER_HOME/ \
-Dspring.config.location=file://$APISERVER_HOME/conf/application.properties \
-Dspring.config.location=file://$APISERVER_HOME/conf/application-prod.yml \
-Dspring.profiles.active=prod \
org.springframework.boot.loader.PropertiesLauncher > /dev/null 2>&1 &

PID=$!
echo "$PID" > pid;
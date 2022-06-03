#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-filebeat

## define helm application name.
APP_NAME=filebeat

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

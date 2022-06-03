#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-logstash

## define helm application name.
APP_NAME=dataroaster-logstash

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

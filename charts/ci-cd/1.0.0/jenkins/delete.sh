#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-jenkins

## define helm application name.
APP_NAME=jenkins

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

#!/bin/bash

set -x


## define namespace
NAMESPACE=dataroaster-harbor

## define helm application name.
APP_NAME=harbor

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};


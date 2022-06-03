#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-trino

## define helm application name.
APP_NAME=trino

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

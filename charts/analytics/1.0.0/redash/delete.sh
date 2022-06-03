#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-redash

## define helm application name.
APP_NAME=redash

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-hivemetastore

## define helm application name.
APP_NAME=hivemetastore-mysql

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

#!/bin/bash

## define namespace
NAMESPACE=dataroaster-argo-workflow

## define helm application name.
APP_NAME=argo-workflow

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};


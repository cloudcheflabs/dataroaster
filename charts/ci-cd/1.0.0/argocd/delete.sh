#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-argocd

## define helm application name.
APP_NAME=argocd

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

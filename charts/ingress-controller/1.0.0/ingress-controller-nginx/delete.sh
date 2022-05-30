#!/bin/bash

set -x

## define namespace
NAMESPACE=ingress-nginx

## define helm application name.
APP_NAME=ingress-nginx

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

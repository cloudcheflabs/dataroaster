#!/bin/bash

set -x


## define namespace
NAMESPACE=dataroaster-prom-stack

## define helm application name.
APP_NAME=prom-stack

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

# delete metrics server.
kubectl delete -f metrics-server.yaml;

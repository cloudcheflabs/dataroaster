#!/bin/bash

set -x

cd {{ tempDirectory }};


## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=prom-stack

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

# delete metrics server.
kubectl delete -f metrics-server.yaml --kubeconfig={{ kubeconfig }};

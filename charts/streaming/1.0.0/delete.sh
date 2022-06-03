#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-kafka

## define helm application name.
APP_NAME=kafka

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE};

## delete kafka client if exists.
kubectl delete po kafka-client -n ${NAMESPACE};
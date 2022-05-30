#!/bin/bash

set -x

cd {{ tempDirectory }};

## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=kafka

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

## delete kafka client if exists.
kubectl delete po kafka-client -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};
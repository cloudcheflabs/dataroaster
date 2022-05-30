#!/bin/bash

set -x

## define namespace
NAMESPACE={{ redashNamespace }}

## define helm application name.
APP_NAME=redash

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

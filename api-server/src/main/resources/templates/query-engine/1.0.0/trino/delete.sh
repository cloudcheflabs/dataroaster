#!/bin/bash

set -x

## define namespace
NAMESPACE={{ trinoNamespace }}

## define helm application name.
APP_NAME=trino

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

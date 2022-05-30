#!/bin/bash

set -x

## define namespace
NAMESPACE={{ jenkinsNamespace }}

## define helm application name.
APP_NAME=jenkins

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

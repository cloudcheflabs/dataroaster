#!/bin/bash

set -x

## define namespace
NAMESPACE={{ argocdNamespace }}

## define helm application name.
APP_NAME=argocd

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

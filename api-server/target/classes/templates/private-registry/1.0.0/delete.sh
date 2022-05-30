#!/bin/bash

set -x

cd {{ tempDirectory }};

## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=harbor

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};


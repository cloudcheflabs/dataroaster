#!/bin/bash

set -x

## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=hivemetastore-metastore

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

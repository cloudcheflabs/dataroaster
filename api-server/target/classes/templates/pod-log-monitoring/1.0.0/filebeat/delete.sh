#!/bin/bash

set -x

## define namespace
NAMESPACE={{ filebeatNamespace }}

## define helm application name.
APP_NAME=filebeat

## uninstall.
helm uninstall ${APP_NAME} -n ${NAMESPACE} \
--kubeconfig={{ kubeconfig }};

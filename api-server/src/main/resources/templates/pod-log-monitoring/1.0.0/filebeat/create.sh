#!/bin/bash

set -x


## define namespace
NAMESPACE={{ filebeatNamespace }}

## define helm application name.
APP_NAME=filebeat

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};
#!/bin/bash

set -x


## define namespace
NAMESPACE={{ trinoNamespace }}

## define helm application name.
APP_NAME=trino

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};


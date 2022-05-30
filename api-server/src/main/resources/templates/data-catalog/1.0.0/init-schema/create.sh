#!/bin/bash

set -x


## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=hivemetastore-init-schema

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};


# wait.
kubectl wait --namespace ${NAMESPACE} \
--for=condition=complete job/hive-initschema \
--timeout=120s \
--kubeconfig={{ kubeconfig }};
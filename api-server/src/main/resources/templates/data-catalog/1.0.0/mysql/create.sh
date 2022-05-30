#!/bin/bash

set -x


## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=hivemetastore-mysql

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};



# wait.
kubectl wait --namespace ${NAMESPACE} \
--for=condition=ready pod \
--selector=app=mysql \
--timeout=120s \
--kubeconfig={{ kubeconfig }};
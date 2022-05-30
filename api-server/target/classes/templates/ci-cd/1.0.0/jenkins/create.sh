#!/bin/bash

set -x


## define namespace
NAMESPACE={{ jenkinsNamespace }}

## define helm application name.
APP_NAME=jenkins

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};


# wait for jenkins server.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=jenkins \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};
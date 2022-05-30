#!/bin/bash

set -x


## define namespace
NAMESPACE=dataroaster-argocd

## define helm application name.
APP_NAME=argocd

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./;


# wait for argo server.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=argocd-repo-server \
  --timeout=120s
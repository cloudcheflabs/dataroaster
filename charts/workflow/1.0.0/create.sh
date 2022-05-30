#!/bin/bash

## define namespace
NAMESPACE=dataroaster-argo-workflow

## define helm application name.
APP_NAME=argo-workflow

# install.
helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./;


# wait for a while to initialize argo workflow.
sleep 10


# wait.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=postgres \
  --timeout=120s

kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=workflow-controller \
  --timeout=120s

kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=argo-server \
  --timeout=120s
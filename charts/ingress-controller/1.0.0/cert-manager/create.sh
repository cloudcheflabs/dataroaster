#!/bin/bash

set -x

## define namespace
NAMESPACE=cert-manager

## create namespace.
kubectl create namespace ${NAMESPACE};

# create cert-manager.
kubectl apply -f cert-manager.yaml;

# wait until webhook is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=webhook \
  --timeout=120s

# wait until controller is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

sleep 10

# create issuer.
kubectl apply -f prod-issuer.yaml;
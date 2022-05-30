#!/bin/bash

set -x

## define namespace
NAMESPACE={{ namespace }}

## create namespace.
kubectl create namespace ${NAMESPACE} --kubeconfig={{ kubeconfig }};

# create cert-manager.
kubectl apply -f cert-manager.yaml --kubeconfig={{ kubeconfig }};

# wait until webhook is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=webhook \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};

# wait until controller is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};

sleep 10

# create issuer.
kubectl apply -f prod-issuer.yaml --kubeconfig={{ kubeconfig }};
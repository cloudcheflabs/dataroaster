#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-kafka

## define helm application name.
APP_NAME=kafka

# add repo.
helm repo add bitnami https://charts.bitnami.com/bitnami

# list up versions of kafka.
helm search repo bitnami/kafka --versions

# show values of kafka.
helm show values bitnami/kafka

# install.
helm install \
${APP_NAME} \
bitnami/kafka \
--version 13.0.4 \
--create-namespace \
--namespace ${NAMESPACE} \
--values dataroaster-values.yaml;

# wait for a while to initialize kafka.
sleep 5

# wait until kafka is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/name=kafka \
  --timeout=120s
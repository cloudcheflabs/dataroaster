#!/bin/bash

set -x


## define namespace
NAMESPACE=dataroaster-logstash

## define helm application name.
APP_NAME=dataroaster-logstash

# install.
helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./;

# wait.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app=logstash \
  --timeout=120s
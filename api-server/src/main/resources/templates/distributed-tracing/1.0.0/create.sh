#!/bin/bash

set -x

cd {{ tempDirectory }};


## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=jaeger

# add repos.
helm repo add jaegertracing https://jaegertracing.github.io/helm-charts;

helm repo add elastic https://helm.elastic.co
helm show values elastic/elasticsearch

helm repo add bitnami https://charts.bitnami.com/bitnami
helm show values bitnami/kafka

# install.
helm install \
${APP_NAME} \
jaegertracing/jaeger \
--version 0.46.0 \
--create-namespace \
--namespace ${NAMESPACE} \
--values dataroaster-jaeger.yaml \
--kubeconfig={{ kubeconfig }};


# wait until kafka is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=kafka \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};

# wait until ingester is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=ingester \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};

# wait until collector is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=collector \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};

# wait until query is ready.
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=query \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};
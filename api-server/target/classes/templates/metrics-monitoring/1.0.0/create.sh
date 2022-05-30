#!/bin/bash

set -x

cd {{ tempDirectory }};

# install metrics server.
kubectl apply -f metrics-server.yaml --kubeconfig={{ kubeconfig }};


# patch metrics server deployment.
## add --kubelet-insecure-tls in args.
kubectl patch deploy metrics-server \
--kubeconfig={{ kubeconfig }} \
-n kube-system \
 -p '{"spec": {"template": {"spec": {"containers": [{"args": ["--kubelet-insecure-tls", "--cert-dir=/tmp", "--secure-port=4443", "--kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname", "--kubelet-use-node-status-port"], "name": "metrics-server"}]}}}}'


## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=prom-stack


# add repo.
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts;
helm repo add stable https://charts.helm.sh/stable;
helm repo update;


# install.
helm install \
prom-stack \
prometheus-community/kube-prometheus-stack \
--version 12.2.4 \
--create-namespace \
--namespace ${NAMESPACE} \
--values dataroaster-prom-values.yaml \
--kubeconfig={{ kubeconfig }};

## patch grafana service to set the type of LoadBalancer.
kubectl patch svc prom-stack-grafana \
-n ${NAMESPACE} \
--kubeconfig={{ kubeconfig }} \
-p '{"spec": {"type": "LoadBalancer"}}';

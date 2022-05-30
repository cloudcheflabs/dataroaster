#!/bin/bash

set -x


## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=ingress-nginx

# add repo
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update

## create namespace.
kubectl create namespace ${NAMESPACE} --kubeconfig={{ kubeconfig }};

helm install \
--namespace ${NAMESPACE} \
${APP_NAME} \
ingress-nginx/ingress-nginx \
--version 3.32.0 \
--kubeconfig={{ kubeconfig }};


# wait until it is ready to run the next command
kubectl wait --namespace ${NAMESPACE} \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s \
  --kubeconfig={{ kubeconfig }};

## detect installed version.
POD_NAME=$(kubectl get pods -l app.kubernetes.io/name=ingress-nginx -n ${NAMESPACE} -o jsonpath='{.items[0].metadata.name}' --kubeconfig={{ kubeconfig }})
kubectl exec -it $POD_NAME -n ${NAMESPACE} -- /nginx-ingress-controller --version --kubeconfig={{ kubeconfig }};

## get external ingress nginx service ip of the type of loadbalancer.
kubectl get svc -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};
#!/bin/bash

set -x


## define namespace
NAMESPACE={{ redashNamespace }}

## define helm application name.
APP_NAME=redash

helm install \
--create-namespace \
--namespace ${NAMESPACE} \
${APP_NAME} \
--values dataroaster-values.yaml \
./ \
--kubeconfig={{ kubeconfig }};

# wait for a while to initialize redash.
sleep 5

# wait.
kubectl wait --namespace ${NAMESPACE} \
--for=condition=ready pod \
--selector=app=redash \
--timeout=120s \
--kubeconfig={{ kubeconfig }};

# sleep for making sure to be ready to create db.
sleep 10

# create tables.
kubectl --kubeconfig={{ kubeconfig }} \
exec -it -n ${NAMESPACE} \
$(kubectl get po -l app=redash -n ${NAMESPACE} -o jsonpath={.items[0].metadata.name}) \
-c server -- /app/bin/docker-entrypoint create_db;
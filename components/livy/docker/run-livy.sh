#!/bin/bash

set -eux;

echo "adding token to kubeconfig...";
TOKEN=$(cat /var/run/secrets/kubernetes.io/serviceaccount/token);
REPLACE="s/<token-here>/${TOKEN}/g";
echo "$REPLACE";
sed -i $REPLACE /opt/livy/.kube/config;
cat /opt/livy/.kube/config;

echo "starting livy...";
bin/livy-server start;
tail -f logs/livy-*-server.out;
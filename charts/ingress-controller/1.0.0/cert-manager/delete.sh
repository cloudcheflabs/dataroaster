#!/bin/bash

set -x

## define namespace
NAMESPACE=cert-manager

# delete issuer.
kubectl delete -f prod-issuer.yaml;

# delete cert-manager.
kubectl delete -f cert-manager.yaml;

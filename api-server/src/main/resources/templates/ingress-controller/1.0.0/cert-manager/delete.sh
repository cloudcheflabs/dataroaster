#!/bin/bash

set -x

## define namespace
NAMESPACE={{ namespace }}

# delete issuer.
kubectl delete -f prod-issuer.yaml --kubeconfig={{ kubeconfig }};

# delete cert-manager.
kubectl delete -f cert-manager.yaml --kubeconfig={{ kubeconfig }};

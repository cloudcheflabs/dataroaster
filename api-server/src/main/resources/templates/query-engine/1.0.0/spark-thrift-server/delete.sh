#!/bin/bash

set -x

## define namespace
NAMESPACE={{ sparkThriftServerNamespace }};

# delete spark thrift server driver.
kubectl delete po --selector=spark-role=driver -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

# delete loadbalancer service.
kubectl delete svc spark-thrift-server-service -n ${NAMESPACE} --kubeconfig={{ kubeconfig }};

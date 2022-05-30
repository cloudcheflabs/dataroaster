#!/bin/bash

set -x

## define namespace
NAMESPACE=dataroaster-spark-thrift-server;

# delete spark thrift server driver.
kubectl delete po --selector=spark-role=driver -n ${NAMESPACE};

# delete loadbalancer service.
kubectl delete svc spark-thrift-server-service -n ${NAMESPACE};

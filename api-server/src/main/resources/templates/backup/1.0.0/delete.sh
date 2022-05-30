#!/bin/bash

set -x

cd {{ tempDirectory }};

## define namespace
NAMESPACE={{ namespace }}

## uninstall.
velero uninstall --force \
--namespace ${NAMESPACE} \
--kubeconfig={{ kubeconfig }};


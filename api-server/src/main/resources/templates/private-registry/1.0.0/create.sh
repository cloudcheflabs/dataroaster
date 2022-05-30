#!/bin/bash

set -x

cd {{ tempDirectory }};

## define namespace
NAMESPACE={{ namespace }}

## define helm application name.
APP_NAME=harbor


# add repo.
helm repo add harbor https://helm.goharbor.io;
helm repo update;


# install harbor.
helm install \
${APP_NAME} \
harbor/harbor \
--version 1.5.5 \
--create-namespace \
--namespace ${NAMESPACE} \
--values dataroaster-harbor.yaml \
--kubeconfig={{ kubeconfig }};


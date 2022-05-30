#!/bin/bash

## define namespace
NAMESPACE={{ jupyterhubNamespace }}

## define helm application name.
APP_NAME=jupyterhub

# uninstall.
helm uninstall $APP_NAME --namespace $NAMESPACE --kubeconfig={{ kubeconfig }};


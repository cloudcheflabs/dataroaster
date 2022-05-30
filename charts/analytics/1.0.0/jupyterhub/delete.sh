#!/bin/bash

## define namespace
NAMESPACE=dataroaster-jupyterhub

## define helm application name.
APP_NAME=jupyterhub

# uninstall.
helm uninstall $APP_NAME --namespace $NAMESPACE;


#!/bin/bash

set -eux;

java \
-cp ./helm-operator-*-fat.jar \
com.cloudcheflabs.dataroaster.operators.helm.HelmOperator;
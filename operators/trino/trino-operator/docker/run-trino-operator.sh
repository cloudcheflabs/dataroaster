#!/bin/bash

set -eux;

java \
-cp ./trino-operator-*-fat.jar \
com.cloudcheflabs.dataroaster.operators.trino.TrinoOperator;
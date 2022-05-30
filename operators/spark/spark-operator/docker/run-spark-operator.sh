#!/bin/bash

set -eux;

java \
-cp ./spark-operator-*-fat.jar \
com.cloudcheflabs.dataroaster.operators.spark.SparkOperator;
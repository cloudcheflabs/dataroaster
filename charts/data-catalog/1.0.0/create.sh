#!/bin/bash

set -x

cd mysql;
./create.sh;

cd ../init-schema;
./create.sh;

cd ../metastore;
./create.sh;


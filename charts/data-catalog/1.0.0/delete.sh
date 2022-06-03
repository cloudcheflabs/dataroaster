#!/bin/bash

set -x

cd metastore;
./delete.sh;

cd ../mysql;
./delete.sh;

cd ../init-schema;
./delete.sh;

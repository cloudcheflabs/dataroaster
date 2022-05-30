#!/bin/bash

set -x

cd {{ tempDirectory }};

cd metastore;
./delete.sh;

cd ../mysql;
./delete.sh;

cd ../init-schema;
./delete.sh;

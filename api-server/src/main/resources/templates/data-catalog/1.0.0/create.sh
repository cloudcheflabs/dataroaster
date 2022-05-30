#!/bin/bash

set -x

cd {{ tempDirectory }};

cd mysql;
./create.sh;

cd ../init-schema;
./create.sh;

cd ../metastore;
./create.sh;


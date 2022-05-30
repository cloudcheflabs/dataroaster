#!/bin/bash

set -x

cd {{ tempDirectory }};

cd spark-thrift-server;
./create.sh;


cd ../trino;
./create.sh;


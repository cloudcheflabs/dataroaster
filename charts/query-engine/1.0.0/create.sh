#!/bin/bash

set -x

cd spark-thrift-server;
./create.sh;


cd ../trino;
./create.sh;


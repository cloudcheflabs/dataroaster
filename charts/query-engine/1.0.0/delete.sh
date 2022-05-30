#!/bin/bash

set -x

cd spark-thrift-server;
./delete.sh;

cd ../trino;
./delete.sh;

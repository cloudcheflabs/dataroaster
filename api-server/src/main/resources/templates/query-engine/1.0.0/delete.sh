#!/bin/bash

set -x

cd {{ tempDirectory }};

cd spark-thrift-server;
./delete.sh;

cd ../trino;
./delete.sh;

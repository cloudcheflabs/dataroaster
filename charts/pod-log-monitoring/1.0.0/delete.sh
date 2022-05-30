#!/bin/bash

set -x

cd filebeat;
./delete.sh;

cd ../logstash;
./delete.sh;

#!/bin/bash

set -x

cd logstash;
./create.sh;

cd ../filebeat;
./create.sh;


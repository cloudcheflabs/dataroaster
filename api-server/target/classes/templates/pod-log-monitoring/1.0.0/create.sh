#!/bin/bash

set -x

cd {{ tempDirectory }};

cd logstash;
./create.sh;

cd ../filebeat;
./create.sh;


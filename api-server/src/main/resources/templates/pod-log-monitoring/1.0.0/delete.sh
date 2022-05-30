#!/bin/bash

set -x

cd {{ tempDirectory }};

cd filebeat;
./delete.sh;

cd ../logstash;
./delete.sh;

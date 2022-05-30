#!/bin/bash

set -x

cd redash;
./create.sh;

cd ../jupyterhub;
./create.sh;


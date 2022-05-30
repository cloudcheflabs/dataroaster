#!/bin/bash

set -x

cd {{ tempDirectory }};

cd redash;
./create.sh;

cd ../jupyterhub;
./create.sh;


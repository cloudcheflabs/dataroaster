#!/bin/bash

set -x

cd {{ tempDirectory }};

cd redash;
./delete.sh;

cd ../jupyterhub;
./delete.sh;

#!/bin/bash

set -x

cd redash;
./delete.sh;

cd ../jupyterhub;
./delete.sh;

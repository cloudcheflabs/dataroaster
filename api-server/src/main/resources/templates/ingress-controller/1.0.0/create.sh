#!/bin/bash

set -x

cd {{ tempDirectory }};

# create ingress controller.
cd ingress-controller-nginx;
./create.sh;

# create cert-manager.
cd ../cert-manager;
./create.sh;


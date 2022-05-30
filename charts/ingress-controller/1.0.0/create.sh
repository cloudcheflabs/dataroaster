#!/bin/bash

set -x

# create ingress controller.
cd ingress-controller-nginx;
./create.sh;

# create cert-manager.
cd ../cert-manager;
./create.sh;


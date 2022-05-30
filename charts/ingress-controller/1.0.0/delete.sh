#!/bin/bash

set -x

# delete cert-manager.
cd cert-manager;
./delete.sh;

# delete ingress controller.
cd ../ingress-controller-nginx;
./delete.sh;

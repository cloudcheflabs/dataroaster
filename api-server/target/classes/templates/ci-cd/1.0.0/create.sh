#!/bin/bash

set -x

cd {{ tempDirectory }};

# create argocd.
cd argocd;
./create.sh;

# create jenkins.
cd ../jenkins;
./create.sh;


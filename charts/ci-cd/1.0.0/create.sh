#!/bin/bash

set -x

# create argocd.
cd argocd;
./create.sh;

# create jenkins.
cd ../jenkins;
./create.sh;


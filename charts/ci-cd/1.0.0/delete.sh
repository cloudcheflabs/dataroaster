#!/bin/bash

set -x

# delete jenkins.
cd jenkins;
./delete.sh;

# delete argocd.
cd ../argocd;
./delete.sh;

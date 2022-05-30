#!/bin/bash

set -x

cd {{ tempDirectory }};

# delete jenkins.
cd jenkins;
./delete.sh;

# delete argocd.
cd ../argocd;
./delete.sh;

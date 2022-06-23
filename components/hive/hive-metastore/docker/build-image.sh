#!/bin/bash

set -e

REPONAME=cloudcheflabs
TAG=hivemetastore:v3.0.0

docker build -t $TAG .

# Tag and push to the public docker repository.
docker tag $TAG $REPONAME/$TAG
docker push $REPONAME/$TAG
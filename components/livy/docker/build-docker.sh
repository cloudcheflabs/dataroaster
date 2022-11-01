#!/bin/bash


set -e -x

export LIVY_IMAGE=cloudcheflabs/livy:0.7.1

for i in "$@"
do
case $i in
    --image=*)
    LIVY_IMAGE="${i#*=}"
    shift
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "LIVY_IMAGE = ${LIVY_IMAGE}"


set +e -x

# build docker.
## remove dcker image.
docker rmi -f $(docker images -a | grep livy | awk '{print $3}')

set -e -x

cd ../;

## build.
docker build \
-t ${LIVY_IMAGE} \
./docker;


# push docker image.
docker push ${LIVY_IMAGE};

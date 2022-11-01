#!/bin/bash


set -e -x

export METABASE_IMAGE=cloudcheflabs/metabase:v0.44.3

for i in "$@"
do
case $i in
    --image=*)
    METABASE_IMAGE="${i#*=}"
    shift
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "METABASE_IMAGE = ${METABASE_IMAGE}"


set +e -x

# build docker.
## remove dcker image.
docker rmi -f $(docker images -a | grep metabase | awk '{print $3}')

set -e -x

cd ../;

## build.
docker build \
-t ${METABASE_IMAGE} \
./docker;


# push docker image.
docker push ${METABASE_IMAGE};

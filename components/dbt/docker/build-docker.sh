#!/bin/bash


set -e -x

export DBT_IMAGE=cloudcheflabs/dbt:0.21.0

for i in "$@"
do
case $i in
    --image=*)
    DBT_IMAGE="${i#*=}"
    shift
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "DBT_IMAGE = ${DBT_IMAGE}"


set +e -x

# build docker.
## remove docker image.
docker rmi -f $(docker images -a | grep dbt | awk '{print $3}')

set -e -x

cd ../;

## build.
docker build \
-t ${DBT_IMAGE} \
./docker;


# push docker image.
docker push ${DBT_IMAGE};

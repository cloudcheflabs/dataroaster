#!/bin/bash

export SPARK_VERSION=3.0.3;
export SPARK_OPERATOR_VERSION=latest
export SPARK_OPERATOR_FAT_JAR_VERSION=4.1.0-SNAPSHOT
for i in "$@"
do
case $i in
    --spark.version=*)
    SPARK_VERSION="${i#*=}"
    shift
    ;;
    --spark.operator.version=*)
    SPARK_OPERATOR_VERSION="${i#*=}"
    shift
    ;;
    --spark.operator.fat.jar.version=*)
    SPARK_OPERATOR_FAT_JAR_VERSION="${i#*=}"
    shift
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "SPARK_VERSION                   = ${SPARK_VERSION}"
echo "SPARK_OPERATOR_VERSION          = ${SPARK_OPERATOR_VERSION}"
echo "SPARK_OPERATOR_FAT_JAR_VERSION  = ${SPARK_OPERATOR_FAT_JAR_VERSION}"


# build all.
cd ../../..;
mvn -e -DskipTests=true clean install;

# move to spark operator dir.
cd operators/spark/spark-operator;

# build spark operator fat jar.
mvn -e -DskipTests=true clean install shade:shade;

# spark operator fat jar.
export SPARK_OPERATOR_JAR=spark-operator-${SPARK_OPERATOR_FAT_JAR_VERSION}-fat.jar;

# add fat jar to docker directory.
cp target/$SPARK_OPERATOR_JAR docker;

# build docker.
## remove spark operator image.
docker rmi -f $(docker images -a | grep spark-operator | awk '{print $3}')

## image.
export SPARK_OPERATOR_IMAGE=cloudcheflabs/spark-operator:${SPARK_OPERATOR_VERSION}

## build.
docker build \
--build-arg SPARK_VERSION=${SPARK_VERSION} \
--build-arg SPARK_OPERATOR_JAR=${SPARK_OPERATOR_JAR} \
-t ${SPARK_OPERATOR_IMAGE} \
./docker;


## remove spark operator fat jar from docker directory.
rm -rf docker/$SPARK_OPERATOR_JAR;

# push docker image.
docker push ${SPARK_OPERATOR_IMAGE};
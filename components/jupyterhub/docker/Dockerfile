FROM docker.io/bitnami/jupyter-base-notebook:1.5.0-debian-11-r8

USER 0
RUN apt --allow-unauthenticated -o Acquire::Check-Valid-Until=false update && \
    apt install --reinstall build-essential -y && \
    apt install -y apt-utils && \
    apt -y install gcc && \
    apt install libsasl2-2 libsasl2-dev libsasl2-modules -y && \
    apt install -y curl && \
    apt install -y openjdk-11-jdk

# install python.
RUN apt install python3 python3-pip -y

# print python version
RUN python --version

RUN pip install 'pyhive[hive]' && \
    pip install 'pyhive[trino]'

RUN set -x && \
    export SPARK_BASE=/opt/spark && \
    export SPARK_VERSION=3.0.3 && \
    export SPARK_HOME=${SPARK_BASE}/spark-${SPARK_VERSION} && \
    mkdir -p ${SPARK_BASE} && \
    cd ${SPARK_BASE} && \
    curl -L -O https://github.com/cloudcheflabs/spark/releases/download/v${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-custom-spark.tgz && \
    tar zxvf spark-${SPARK_VERSION}-bin-custom-spark.tgz  && \
    mv spark-${SPARK_VERSION}-bin-custom-spark spark-${SPARK_VERSION} && \
    rm -rf spark-${SPARK_VERSION}*.tgz && \
    touch /etc/profile.d/spark.sh && \
    echo "export SPARK_HOME=${SPARK_HOME}" >> /etc/profile.d/spark.sh && \
    echo "export PATH=$PATH:$SPARK_HOME/bin" >> /etc/profile.d/spark.sh


USER 1001
WORKDIR $HOME

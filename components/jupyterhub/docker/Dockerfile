FROM jupyterhub/k8s-singleuser-sample:1.1.3

USER 0
RUN apt-get --allow-unauthenticated -o Acquire::Check-Valid-Until=false update && \
    apt-get install --reinstall build-essential -y && \
    apt-get install -y apt-utils && \
    apt-get -y install gcc && \
    apt-get install libsasl2-2 libsasl2-dev libsasl2-modules -y && \
    apt-get install -y curl && \
    apt-get install -y openjdk-8-jdk

RUN /opt/conda/bin/pip install 'pyhive[hive]' && \
    /opt/conda/bin/pip install 'pyhive[trino]'

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
    echo "export PATH=$PATH:$SPARK_HOME/bin" >> /etc/profile.d/spark.sh && \
    source /etc/profile


USER jovyan
WORKDIR $HOME
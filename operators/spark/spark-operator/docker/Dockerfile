FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;

# install spark.
ARG SPARK_VERSION
ENV SPARK_BASE /opt/spark
ENV SPARK_HOME ${SPARK_BASE}/spark-$SPARK_VERSION
RUN set -eux; \
    mkdir -p ${SPARK_BASE}; \
    cd ${SPARK_BASE}; \
    curl -L -O https://github.com/cloudcheflabs/spark/releases/download/v${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-custom-spark.tgz; \
    tar zxvf spark-${SPARK_VERSION}-bin-custom-spark.tgz; \
    mv spark-${SPARK_VERSION}-bin-custom-spark spark-${SPARK_VERSION}; \
    rm -rf spark-${SPARK_VERSION}*.tgz;

# export spark bin to PATH.
ENV PATH="$PATH:$SPARK_HOME/bin"

# install maven.
ENV MAVEN_HOME /opt/maven
RUN set -eux; \
    mkdir -p ${MAVEN_HOME}; \
    cd ${MAVEN_HOME}; \
    curl -L -O https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz; \
    tar zxvf apache-maven-3.8.6-bin.tar.gz; \
    cp -R apache-maven-3.8.6/* .; \
    rm -rf apache-maven-3.8.6 apache-maven-3.8.6-bin.tar.gz;

# export maven home to path.
ENV PATH="$PATH:$MAVEN_HOME/bin"


# install mc.
RUN set -eux; \
    yum install wget -y; \
    wget https://dl.min.io/client/mc/release/linux-amd64/mc; \
    cp mc /usr/local/bin; \
    chmod +x /usr/local/bin/mc;


# add kubernetes repo.
ADD kubernetes.repo /etc/yum.repos.d

# install kubectl.
RUN yum install kubectl -y;

# install spark operator.
ENV SPARK_OPERATOR_HOME /opt/spark-operator
ENV SPARK_OPERATOR_USER spark_operator

RUN useradd -ms /bin/bash -d ${SPARK_OPERATOR_HOME} ${SPARK_OPERATOR_USER}

# add pom.xml to download spark deps jars.
ADD pom.xml ${SPARK_OPERATOR_HOME}

# add spark operator jar.
ARG SPARK_OPERATOR_JAR
ADD ${SPARK_OPERATOR_JAR} ${SPARK_OPERATOR_HOME}

# add spark run shell.
ADD run-spark-operator.sh ${SPARK_OPERATOR_HOME}

# add permissions.
RUN chmod +x ${SPARK_OPERATOR_HOME}/run-spark-operator.sh
RUN chown ${SPARK_OPERATOR_USER}: -R ${SPARK_OPERATOR_HOME}

# change work directory.
USER ${SPARK_OPERATOR_USER}
WORKDIR ${SPARK_OPERATOR_HOME}

# download dependency jars to run spark job.
RUN mvn -e clean install;

# make kubeconfig directory.
RUN mkdir -p ${SPARK_OPERATOR_HOME}/.kube

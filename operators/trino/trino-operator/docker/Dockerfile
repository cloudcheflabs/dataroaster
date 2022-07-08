FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install trino operator.
ENV TRINO_OPERATOR_HOME /opt/trino-operator
ENV TRINO_OPERATOR_USER trino

RUN useradd -ms /bin/bash -d ${TRINO_OPERATOR_HOME} ${TRINO_OPERATOR_USER}

# add trino operator jar.
ARG TRINO_OPERATOR_JAR
ADD ${TRINO_OPERATOR_JAR} ${TRINO_OPERATOR_HOME}

# add trino run shell.
ADD run-trino-operator.sh ${TRINO_OPERATOR_HOME}

# add permissions.
RUN chmod +x ${TRINO_OPERATOR_HOME}/*.sh
RUN chown ${TRINO_OPERATOR_USER}: -R ${TRINO_OPERATOR_HOME}

# change work directory.
USER ${TRINO_OPERATOR_USER}
WORKDIR ${TRINO_OPERATOR_HOME}

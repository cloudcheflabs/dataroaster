FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install trino gateway.
ENV TRINO_GATEWAY_HOME /opt/trino-gateway
ENV TRINO_GATEWAY_USER trino

RUN useradd -ms /bin/bash -d ${TRINO_GATEWAY_HOME} ${TRINO_GATEWAY_USER}


# add trino gateway jar.
ARG TRINO_GATEWAY_JAR
ADD ${TRINO_GATEWAY_JAR} ${TRINO_GATEWAY_HOME}

# add db schema sql.
ARG DB_SCHEMA
ADD ${DB_SCHEMA} ${TRINO_GATEWAY_HOME}

# make conf directory.
RUN mkdir -p ${TRINO_GATEWAY_HOME}/conf

# add trino run shell.
ADD run-trino-gateway.sh ${TRINO_GATEWAY_HOME}
ADD create-db-schema.sh ${TRINO_GATEWAY_HOME}

# add permissions.
RUN chmod +x ${TRINO_GATEWAY_HOME}/*.sh
RUN chown ${TRINO_GATEWAY_USER}: -R ${TRINO_GATEWAY_HOME}

# change work directory.
USER ${TRINO_GATEWAY_USER}
WORKDIR ${TRINO_GATEWAY_HOME}

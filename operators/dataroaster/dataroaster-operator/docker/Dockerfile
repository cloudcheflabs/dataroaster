FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all

# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install dataroaster operator.
ENV DATAROASTER_OPERATOR_HOME /opt/dataroaster-operator
ENV DATAROASTER_OPERATOR_USER dataroaster

RUN useradd -ms /bin/bash -d ${DATAROASTER_OPERATOR_HOME} ${DATAROASTER_OPERATOR_USER}

# add db schema sql.
ARG DATAROASTER_DB_SCHEMA
ADD ${DATAROASTER_DB_SCHEMA} ${DATAROASTER_OPERATOR_HOME}


# add dataroaster operator jar.
ARG DATAROASTER_OPERATOR_JAR
ADD ${DATAROASTER_OPERATOR_JAR} ${DATAROASTER_OPERATOR_HOME}

# make conf directory.
RUN mkdir -p ${DATAROASTER_OPERATOR_HOME}/conf

# add run shell.
ADD run-dataroaster-operator.sh ${DATAROASTER_OPERATOR_HOME}
ADD create-db-schema.sh ${DATAROASTER_OPERATOR_HOME}

# add permissions.
RUN chmod +x ${DATAROASTER_OPERATOR_HOME}/*.sh
RUN chown ${DATAROASTER_OPERATOR_USER}: -R ${DATAROASTER_OPERATOR_HOME}

# change work directory.
USER ${DATAROASTER_OPERATOR_USER}
WORKDIR ${DATAROASTER_OPERATOR_HOME}

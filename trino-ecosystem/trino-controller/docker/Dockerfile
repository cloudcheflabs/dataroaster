FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install trino controller.
ENV TRINO_CONTROLLER_HOME /opt/trino-controller
ENV TRINO_CONTROLLER_USER trino

RUN useradd -ms /bin/bash -d ${TRINO_CONTROLLER_HOME} ${TRINO_CONTROLLER_USER}

# add trino controller jar.
ARG TRINO_CONTROLLER_JAR
ADD ${TRINO_CONTROLLER_JAR} ${TRINO_CONTROLLER_HOME}

# add trino run shell.
ADD run-trino-controller.sh ${TRINO_CONTROLLER_HOME}

# add permissions.
RUN chmod +x ${TRINO_CONTROLLER_HOME}/*.sh
RUN chown ${TRINO_CONTROLLER_USER}: -R ${TRINO_CONTROLLER_HOME}

# change work directory.
USER ${TRINO_CONTROLLER_USER}
WORKDIR ${TRINO_CONTROLLER_HOME}

FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;


# install helm operator.
ENV HELM_OPERATOR_HOME /opt/helm-operator
ENV HELM_OPERATOR_USER helm

RUN useradd -ms /bin/bash -d ${HELM_OPERATOR_HOME} ${HELM_OPERATOR_USER}


# add helm operator jar.
ARG HELM_OPERATOR_JAR
ADD ${HELM_OPERATOR_JAR} ${HELM_OPERATOR_HOME}

# add helm run shell.
ADD run-helm-operator.sh ${HELM_OPERATOR_HOME}

# add permissions.
RUN chmod +x ${HELM_OPERATOR_HOME}/*.sh
RUN chown ${HELM_OPERATOR_USER}: -R ${HELM_OPERATOR_HOME}

# install helm.
RUN set -eux; \
    curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash; \
    helm

# set kubeconfig env.
ENV KUBECONFIG ${HELM_OPERATOR_HOME}/.kube/config

# change work directory.
USER ${HELM_OPERATOR_USER}
WORKDIR ${HELM_OPERATOR_HOME}

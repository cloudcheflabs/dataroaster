FROM cloudcheflabs/spark:v3.2.2


# env.
ENV LIVY_HOME /opt/livy
ENV LIVY_USER livy
ENV KUBECONFIG ${LIVY_HOME}/.kube/config


RUN useradd -ms /bin/bash -d ${LIVY_HOME} ${LIVY_USER}


# install livy.
RUN set -eux; \
    apt install -y unzip curl; \
    mkdir -p ${LIVY_HOME}/.kube; \
    cd ${LIVY_HOME}; \
    curl -L -O https://dlcdn.apache.org/incubator/livy/0.7.1-incubating/apache-livy-0.7.1-incubating-bin.zip; \
    unzip apache-livy-0.7.1-incubating-bin.zip; \
    cp -rv apache-livy-0.7.1-incubating-bin/* .; \
    rm -rf apache-livy-0.7.1-incubating-bin/; \
    rm -rf apache-livy-0.7.1-incubating-bin.zip;

# add run shell.
ADD run-livy.sh ${LIVY_HOME}

# add  kubeconfig.
ADD config ${LIVY_HOME}/.kube

# add permissions.
RUN chown ${LIVY_USER}: -R ${LIVY_HOME}

# change work directory.
USER ${LIVY_USER}
RUN chmod +x ${LIVY_HOME}/*.sh
WORKDIR ${LIVY_HOME}

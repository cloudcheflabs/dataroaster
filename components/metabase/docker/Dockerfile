FROM centos:7

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN yum install -y tzdata openssl curl ca-certificates fontconfig gzip tar \
    && yum update -y; yum clean all


# install jdk.

RUN set -eux; \
    yum install java-11-openjdk java-11-openjdk-devel -y;

# set time zone.
ENV TZ="Asia/Seoul"

# print date.
RUN echo "current date: $(date)"


# set charset.
RUN sed -i 's/override_install_langs=en_US/#override_install_langs=en_US/g' /etc/yum.conf
RUN yum reinstall -y glibc-common glibc
RUN locale -a
RUN yum install -y sudo tar git rsync gcc gcc-c++ net-tools which iproute man-pages man setuptool ncurses-devel python-devel libevent-expat-devel bzip2-devel
RUN yum groupinstall -y 'development tools'
RUN localedef -f UTF-8 -i ko_KR ko_KR.utf8
ENV LANG=ko_KR.utf8
ENV LC_ALL=ko_KR.utf8
RUN echo -e 'export LANG="ko_KR.UTF-8"\nexport LC_ALL="ko_KR.UTF-8"' > /etc/locale.conf && source /etc/locale.conf
RUN locale -a


# install metabase.
ENV METABASE_HOME /opt/metabase
ENV METABASE_USER metabase

RUN useradd -ms /bin/bash -d ${METABASE_HOME} ${METABASE_USER}


# download metabase jar and trino driver.
RUN set -eux; \
    cd ${METABASE_HOME}; \
    mkdir -p plugins; \
    curl -L -O https://github.com/starburstdata/metabase-driver/releases/download/1.0.6/starburst-1.0.6.metabase-driver.jar; \
    curl -L -O https://downloads.metabase.com/v0.44.3/metabase.jar; \
    mv starburst-1.0.6.metabase-driver.jar plugins/;

# add run shell.
ADD run-metabase.sh ${METABASE_HOME}

# add permissions.
RUN chown ${METABASE_USER}: -R ${METABASE_HOME}

# change work directory.
USER ${METABASE_USER}
RUN chmod +x ${METABASE_HOME}/*.sh
WORKDIR ${METABASE_HOME}

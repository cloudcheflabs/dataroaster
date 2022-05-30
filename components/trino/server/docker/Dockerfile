FROM openjdk:11-slim

ENV APP_HOME /opt/trino-server
ENV TRINO_USER trino
ARG TRINO_VER

RUN useradd -ms /bin/bash -d ${APP_HOME} ${TRINO_USER}

RUN apt-get update && apt-get install -y curl dnsutils netcat python --no-install-recommends \
	&& rm -rf /var/lib/apt/lists/*

RUN set -ex \
    && PACK_NAME=trino-server-${TRINO_VER} \
    && FILE_NAME=${PACK_NAME}.tar.gz \
    && curl -O https://repo1.maven.org/maven2/io/trino/trino-server/${TRINO_VER}/${FILE_NAME} \
    && tar -zxf ${FILE_NAME} \
    && cp -R ${PACK_NAME}/* ${APP_HOME} \
    && mkdir -p ${APP_HOME}/etc/catalog \
    && rm -rf ${FILE_NAME} \
    && rm -rf ${PACK_NAME}

RUN ls -al ${APP_HOME}

RUN chmod a+x -R ${APP_HOME}/bin
RUN chown ${TRINO_USER}: -R ${APP_HOME}

RUN set -ex \
    && echo 'trino soft nofile 131072' >> /etc/security/limits.d/trino.conf \
    && echo 'trino hard nofile 131072' >> /etc/security/limits.d/trino.conf
RUN cat /etc/security/limits.d/trino.conf

## error with trino user: ERROR: [Errno 13] Permission denied: '/opt/trino-server/data/var'
#USER ${TRINO_USER}
WORKDIR ${APP_HOME}

FROM k8s.gcr.io/git-sync/git-sync:v3.6.1

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

ENTRYPOINT []

USER root

ENV DEBIAN_FRONTEND noninteractive

RUN set -eux; \
    apt-get update; \
    apt-get install -y curl --no-install-recommends; \
    apt install python3-pip -y; \
    pip3 install --upgrade pip; \
    pip3 install dbt-core==1.2.1; \
    pip3 install dbt-trino==1.2.2;

# print dbt version.
RUN dbt --version

# print git sync version.
RUN /git-sync --version

# set time zone.
ENV TZ="Asia/Seoul"

# print date.
RUN echo "current date: $(date)"




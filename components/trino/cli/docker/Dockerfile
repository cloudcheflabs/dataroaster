FROM openjdk:8-slim

RUN apt-get update && apt-get install -y curl less --no-install-recommends \
	&& rm -rf /var/lib/apt/lists/*

ARG TRINO_VER
RUN curl -o /opt/trino-cli https://repo1.maven.org/maven2/io/trino/trino-cli/${TRINO_VER}/trino-cli-${TRINO_VER}-executable.jar \
	&& chmod +x /opt/trino-cli

# Remove curl.
RUN apt-get --purge remove -y curl && apt-get autoremove -y

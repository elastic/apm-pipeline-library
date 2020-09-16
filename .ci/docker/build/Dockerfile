FROM adoptopenjdk:8-openj9-bionic as dependencies

COPY . /app
WORKDIR /app
RUN ./mvnw -B --fail-never dependency:go-offline test

FROM adoptopenjdk:8-openj9-bionic
COPY --from=dependencies /root/.m2 /root/.m2

RUN apt-get update -qq -y \
  && apt-get install -qq -y --no-install-recommends git \
  && rm -rf /var/lib/apt/lists/*

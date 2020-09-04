FROM adoptopenjdk:8-openj9-bionic

COPY . /app
WORKDIR /app
RUN ./mvnw dependency:go-offline
ENTRYPOINT ["/app/.mvnw"]

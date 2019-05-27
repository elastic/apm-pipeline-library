#!/usr/bin/env bash
set +x

echo "dummy script"

docker --version || true
java -version || true
go version || true
git version || true
mvn --version || true
node --version || true

JAVA_HOME="${HUDSON_HOME}/.java/java10"
PATH="${JAVA_HOME}/bin:${PATH}"
java -version || true

uname -a || true
df -h || true

docker images || true


#!/usr/bin/env bash

cd resources/scripts/pytest_otel || exit

OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT} \
OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer ${OTEL_TOKEN_ID}" \
make -n test-with-trace || true

OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT} \
OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer ${OTEL_TOKEN_ID}" \
make test-with-trace || true

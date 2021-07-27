#!/usr/bin/env bash

cd resources/scripts/pytest_otel || exit

OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT} \
OTEL_EXPORTER_OTLP_HEADERS="authorization=Bearer ${OTEL_TOKEN_ID}" \
TRACEPARENT="00-${TRACE_ID}-${SPAN_ID}-01" \
make -n test || true

OTEL_EXPORTER_OTLP_ENDPOINT=${OTEL_EXPORTER_OTLP_ENDPOINT} \
OTEL_EXPORTER_OTLP_HEADERS="authorization=Bearer ${OTEL_TOKEN_ID}" \
TRACEPARENT="00-${TRACE_ID}-${SPAN_ID}-01" \
make test || true

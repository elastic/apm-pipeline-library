
Features
--------

pytest-otel plugin for reporting APM traces of tests executed.

[OpenTelemetry](https://opentelemetry.io/docs/)

Requirements
------------

* opentelemetry-api == 1.11.0
* opentelemetry-exporter-otlp == 1.11.0
* opentelemetry-sdk == 1.11.0


Installation
------------

You can install "pytest-otel" via `pip` or using the `setup.py` script.

```
pip install pytest-otel
```

Usage
-----

`pytest_otel` is configured by adding some parameters to the pytest command line. Below are the descriptions:

* --otel-endpoint: URL for the OpenTelemetry server. (Required). Env variable: `OTEL_EXPORTER_OTLP_ENDPOINT`
* --otel-headers: Additional headers to send (i.e.: key1=value1,key2=value2). Env variable: `OTEL_EXPORTER_OTLP_HEADERS`
* --otel-service-name: Name of the service. Env variable: `OTEL_SERVICE_NAME`
* --otel-session-name: Name for the main span.
* --otel-traceparent: Trace parent ID. Env variable: `TRACEPARENT`. See https://www.w3.org/TR/trace-context-1/#trace-context-http-headers-format
* --otel-insecure: Disables TLS. Env variable: `OTEL_EXPORTER_OTLP_INSECURE`

```bash
pytest --otel-endpoint https://otelcollector.example.com:4317 \
       --otel-headers "authorization=Bearer ASWDCcCRFfr" \
       --otel-service-name pytest_otel \
       --otel-session-name='My_Test_cases' \
       --otel-traceparent=00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01 \
       --otel-insecure=False
```

**IMPORTANT**: If you use `--otel-headers` the transaction metadata might expose those arguments
with their values. In order to avoid any credentials to be exposed, it's recommended to use the environment variables.
For instance, given the above example, a similar one with environment variables can be seen below:

```bash
OTEL_EXPORTER_OTLP_ENDPOINT=https://apm.example.com:8200 \
OTEL_EXPORTER_OTLP_HEADERS="authorization=Bearer ASWDCcCRFfr" \
OTEL_SERVICE_NAME=pytest_otel \
TRACEPARENT=00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01 \
OTEL_EXPORTER_OTLP_INSECURE=False \
pytest --otel-session-name='My_Test_cases'
```

Demos
-----

* [Jaeger](https://github.com/elastic/apm-pipeline-library/tree/main/resources/scripts/pytest_otel/docs/demos/jaeger/README.md)
* [Elastic Stack](https://github.com/elastic/apm-pipeline-library/tree/main/resources/scripts/pytest_otel/docs/demos/elastic/README.md)

License
-------

Distributed under the terms of the `Apache License Version 2.0`_ license, "pytest-otel" is free and open source software

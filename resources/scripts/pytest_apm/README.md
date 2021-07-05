
Features
--------

pytest-apm plugin for report APM traces about test executed.


Requirements
------------

* elastic-apm >= 5.7


Installation
------------

You can install "pytest-apm" via `pip` or using the `setup.py` script.

```
git checkout https://github.com/elastic/apm-pipeline-library
cd apm-pipeline-library/resources/scripts
pip install ./pytest_apm
```

Usage
-----

pytest_apm is configured by adding some parameters to the pytest command line here are the descriptions

* --apm-server-url: URL for the APM server. (Required). Env variable `ELASTIC_APM_SERVER_URL`
* --apm-token: Token to access to APM server. (Required if no API key passed). Env variable `ELASTIC_APM_SECRET_TOKEN`
* --apm-api-key: API key to access to APM server. (Required if no token passed). Env variable `ELASTIC_APM_API_KEY`
* --apm-service-name: Name of the service. (Required).
* --apm-custom-context: Custom context for the current transaction in a JSON string.
* --apm-labels: Labels to add to every transaction and span, it is a JSON string.
* --apm-session-name: Name for the Main transaction reported to APM.(Default Session)
* --apm-transaction-mode: Change the way to capture transactiona and spans.
  By default the session is a transaction and the tests are spans.
  If is set, every test is a transaction.
* --apm-trace-parent: The propagation of the trace context.

```bash
cd pytest_apm
pytest --apm-server-url https://apm.example.com:8200 \
       --apm-token ASWDCcCRFfr \
       --apm-service-name pytest_apm \
       --apm-labels '{"var01": "value01","var02": "value02"}' \
       --apm-session-name='My_Test_cases'
```

**IMPORTANT**: If you use `--apm-token` or `--apm-api-key` the transaction metadata might expose those arguments
with their values. In order to avoid any credentials to be exposed, it's recommended to use the environment variables.
For instance, given the above example, the similar one with environment variables can be seen below:

```bash
ELASTIC_APM_SERVER_URL=https://apm.example.com:8200 \
ELASTIC_APM_SECRET_TOKEN=ASWDCcCRFfr \
pytest --apm-service-name pytest_apm \
       --apm-labels '{"var01": "value01","var02": "value02"}' \
       --apm-session-name='My_Test_cases'
```
License
-------

Distributed under the terms of the `Apache License Version 2.0`_ license, "pytest-apm" is free and open source software

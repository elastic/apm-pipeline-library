# Overview

ApmCli.py is a Python script to generate APM traces (transaction and spans) from command line.

# install

You can use `pip` passing the folder of the apm-cli

```
virtualenv -q --python=python3 .venv
. .venv/bin/activate
pip install /resurces/scripts/apm-cli
```

Other option is to use `setup.py` to install the script

```
virtualenv -q --python=python3 .venv
. .venv/bin/activate
cd  /resurces/scripts/apm-cli
python setup.py install -v
```

# Usage

```
usage: apm-cli.py [-h] --apm-server-url APM_SERVER_URL
                  (--apm-token APM_TOKEN | --apm-api-key API_KEY)
                  --service-name SERVICE_NAME [--custom-context CUSTOM_CONTEXT]
                  --transaction-name TRANSACTION_NAME [--transaction-result TRANSACTION_RESULT]
                  [--span-name SPAN_NAME] [--span-type SPAN_TYPE]
                  [--span-subtype SPAN_SUBTYPE] [--span-labels SPAN_LABELS]
                  [--span-action SPAN_ACTION] [--span-command SPAN_CMD]
                  [--parent-transaction-save APM_PARENT_ID_FILE_SAVE]
                  [--parent-transaction-load APM_PARENT_ID_FILE_LOAD]
                  [--parent-transaction APM_PARENT_ID]

Command line tool to generate APM traces. see
https://www.elastic.co/guide/en/apm/agent/python/current/api.html If an arg is
specified in more than one place, then commandline values override environment
variables which override defaults.

optional arguments:
  -h, --help            show this help message and exit
  --apm-server-url APM_SERVER_URL
                        URL for the APM server. [env var: APM_CLI_SERVER_URL]
  --apm-token APM_TOKEN
                        Token to access to APM server. [env var:
                        APM_CLI_TOKEN]
  --apm-api-key API_KEY
                        API key to access to APM server. [env var:
                        APM_CLI_TOKEN_API_KEY]
  --service-name SERVICE_NAME
                        Name of the service. [env var: APM_CLI_SERVICE_NAME]
  --custom-context CUSTOM_CONTEXT
                        Custom context for the current transaction in a JSON
                        string. [env var: APM_CLI_CUSTOM_CONTEXT]

Transaction:
  --transaction-name TRANSACTION_NAME
                        Name of the transaction. [env var:
                        APM_CLI_TRANSACTION_NAME]
  --transaction-result TRANSACTION_RESULT
                        Result for the active transaction. [env var:
                        APM_CLI_TRANSACTION_RESULT]

Span:
  --span-name SPAN_NAME
                        Name of the span. [env var: APM_CLI_SPAN_NAME]
  --span-type SPAN_TYPE
                        Type for the active span. [env var: APM_CLI_SPAN_TYPE]
  --span-subtype SPAN_SUBTYPE
                        SubType for the active span. [env var:
                        APM_CLI_SPAN_SUBTYPE]
  --span-labels SPAN_LABELS
                        Labels for the active span. [env var:
                        APM_CLI_SPAN_LABELS]
  --span-action SPAN_ACTION
                        Action for the active span. [env var:
                        APM_CLI_SPAN_ACTION]
  --span-command SPAN_CMD
                        Command to execute in a shell for the active span.
                        [env var: APM_CLI_SPAN_COMMAND]

Parent Transaction:
  --parent-transaction-save APM_PARENT_ID_FILE_SAVE
                        File to save the parent transaction ID. [env var:
                        APM_CLI_PARENT_TRANSACTION_SAVE]
  --parent-transaction-load APM_PARENT_ID_FILE_LOAD
                        File to load the parent transaction ID. [env var:
                        APM_CLI_PARENT_TRANSACTION_LOAD]
  --parent-transaction APM_PARENT_ID
                        The parent transaction ID. [env var:
                        APM_CLI_PARENT_TRANSACTION]
```

APM connection details, Service name, and transaction name are mandatary so the basic command line is

```
apm-cli.py --apm-server-url https://apm.example.com:8200 \
    --apm-token "${TOKEN}" \
    --service-name "DummyService" \
    --transaction-name "MyTS"
```

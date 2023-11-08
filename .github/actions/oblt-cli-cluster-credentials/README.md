## About

GitHub Action to run the oblt-cli wrapper to retrieve the credentials to connect to the given cluster

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)
  * [output](#output)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Cat indices for the given cluster name using the oblt-cli

...

jobs:
  cat-indices:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-cluster-credentials@current
        with:
          cluster-name: 'edge-oblt'
          github-token: ${{ secrets.PAT_TOKEN }}
          vault-url: ${{ secrets.VAULT_ADDR }}
          vault-role-id: ${{ secrets.VAULT_ROLE_ID }}
          vault-secret-id: ${{ secrets.VAULT_SECRET_ID }}

      ...
      - run: curl -X GET ${ELASTICSEARCH_HOST}/_cat/indices?v -u ${ELASTICSEARCH_USERNAME}:${ELASTICSEARCH_PASSWORD}

...
```

or alternatively if you use `oblt-cli` with `--output-file "${CLUSTER_INFO_FILE}"'` then

```yaml
---
name: Cat indices for the given cluster file using the oblt-cli

...

jobs:
  cat-indices:
    runs-on: ubuntu-latest
    steps:
      ...
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli@current
        with:
          command: 'cluster create ... --output-file "${{ github.workspace }}/cluster-info.json" --wait 15'

      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-cluster-credentials@current
        with:
          cluster-info-file: ${{ github.workspace }}/cluster-info.json
          github-token: ${{ secrets.PAT_TOKEN }}
          vault-url: ${{ secrets.VAULT_ADDR }}
          vault-role-id: ${{ secrets.VAULT_ROLE_ID }}
          vault-secret-id: ${{ secrets.VAULT_SECRET_ID }}

      ...
      - run: curl -X GET ${ELASTICSEARCH_HOST}/_cat/indices?v -u ${ELASTICSEARCH_USERNAME}:${ELASTICSEARCH_PASSWORD}

...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                     | Description                                       |
|-----------------------------|---------|-----------------------------|-------------------------------------------------- |
| `cluster-name `             | String  | Optional                    | The cluster name                                  |
| `cluster-info-file `        | String  | Optional                    | The cluster info file (absolute path)             |
| `github-token`              | String  | Mandatory                   | The GitHub token with permissions fetch releases. |
| `vault-role-id`             | String  | Mandatory                   | The Vault role id.                                |
| `vault-secret-id`           | String  | Mandatory                   | The Vault secret id.                              |
| `vault-url`                 | String  | Mandatory                   | The Vault URL to connect to.                      |

### outputs

Masked environment variables that are available:

* ELASTIC_APM_SERVER_URL
* ELASTIC_APM_JS_SERVER_URL
* ELASTIC_APM_JS_BASE_SERVER_URL
* ELASTIC_APM_SECRET_TOKEN
* ELASTIC_APM_API_KEY
* ELASTICSEARCH_API_TOKEN
* ELASTICSEARCH_HOSTS
* ELASTICSEARCH_HOST
* ELASTICSEARCH_USERNAME
* ELASTICSEARCH_PASSWORD
* FLEET_ELASTICSEARCH_HOST
* FLEET_ENROLLMENT_TOKEN
* FLEET_SERVER_SERVICE_TOKEN
* FLEET_SERVER_POLICY_ID
* FLEET_TOKEN_POLICY_NAME
* FLEET_URL
* KIBANA_HOST
* KIBANA_HOSTS
* KIBANA_FLEET_HOST
* KIBANA_USERNAME
* KIBANA_PASSWORD

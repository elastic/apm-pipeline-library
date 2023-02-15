
## About

GitHub Action to run the updatecli with vault access.

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration


```yaml
---
name: update-specs

on:
  workflow_dispatch:
  schedule:
    - cron: '0 6 * * *'

permissions:
  contents: read

jobs:
  bump:
    runs-on: ubuntu-latest
    steps:

      - uses: actions/checkout@v3

      - uses: elastic/apm-pipeline-library/.github/actions/updatecli@current
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
          pipeline: ./.ci/update-specs.yml

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `pipeline`        | String  |                             | Path to pipeline file. |
| `vaultRoleId`     | String  |                             | The Vault role id. |
| `vaultSecretId`   | String  |                             | The Vault secret id. |
| `vaultUrl`        | String  |                             | The Vault URL to connect to. |
| `command`         | String  | `apply`                     | What updatecli command to run. |
| `dockerRegistry`    | String  | `docker.elastic.co`         | The docker registry. |
| `dockerVaultSecret` | String  | `secret/observability-team/ci/docker-registry/prod` | The Vault secret with the docker auth details. |

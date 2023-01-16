
## About

GitHub Action to export GitHub actions as OpenTelemetry traces.

___

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```
---
name: ci

on:
  pull_request:

permissions:
  contents: read

jobs:
  generate:
    timeout-minutes: 5
    runs-on: ubuntu-latest
    steps:
      ...

```

Then, let's create a new workflow to export the data

```yaml
---
name: OpenTelemetry
on:
  workflow_run:
    workflows: [ ci ]
    types: [ completed ]

jobs:
  publish_results:
    timeout-minutes: 5
    runs-on: ubuntu-latest
    permissions:
      pull-requests: read
      actions: read
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/opentelemetry@current
        with:
          githubToken: ${{ secrets.GITHUB_TOKEN }}
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `githubToken`     | String  | `github.token`              | The GitHub token used to comment out the URL with the report. |
| `vaultRoleId`     | String  | `secrets.VAULT_ROLE_ID`     | The Vault role id. |
| `vaultSecretId`   | String  | `secrets.VAULT_SECRET_ID`   | The Vault secret id. |
| `vaultUrl`        | String  | `secrets.VAULT_ADDR`        | The Vault URL to connect to. |
| `secret`          | String  | `secret/observability-team/ci/jenkins-logs-robots` | The Vault secret. |

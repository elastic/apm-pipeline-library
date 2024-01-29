
## About
GitHub Action will export GitHub actions as OpenTelemetry traces.

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
    workflows: ["*"]
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
| `vaultRoleId`     | String  |                             | The Vault role id. |
| `vaultSecretId`   | String  |                             | The Vault secret id. |
| `vaultUrl`        | String  |                             | The Vault URL to connect to. |
| `secret`          | String  | `secret/observability-team/ci/observability-ci/apm-credentials` | The Vault secret. |

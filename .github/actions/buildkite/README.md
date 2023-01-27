## About

GitHub Action to run a BuildKite pipeline using Vault

___

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration


```yaml
---
name: OpenTelemetry
on:
  workflow_run:
    workflows: [ ci ]
    types: [ completed ]

jobs:
  build-sign:
    timeout-minutes: 5
    runs-on: ubuntu-latest

    steps:

      - name: Run BuildKite pipeline
        uses: .github/actions/buildkite
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
          pipeline: observability-release-helm

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `vaultRoleId`     | String  |                             | The Vault role id. |
| `vaultSecretId`   | String  |                             | The Vault secret id. |
| `vaultUrl`        | String  |                             | The Vault URL to connect to. |
| `secret`          | String  | `secret/observability-team/ci/buildkite-automation` | The Vault secret. |
| `org`             | String  | `elastic`                   | The Buildkite org. |
| `pipeline`        | String  |                             | The Buildkite pipeline to interact with. |
| `waitFor`         | boolean | `false`                     | Whether to wait for the build to finish. |
| `printBuildLogs`  | boolean | `false`                     | Whether to print the build logs. |

## About

GitHub Action to run a Jenkins job using Vault

___

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration


```yaml
---
name: Run In Jenkins
on:
  workflow_run:
    workflows:
      - ci
    types: [ completed ]

jobs:
  build-sign:
    timeout-minutes: 5
    runs-on: ubuntu-latest

    steps:

      - name: Run Jenkins job
        uses: elastic/apm-pipeline-library/.github/actions/jenkins-build@current
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
          jenkins-url: https://beats-ci.elastic.co
          jenkins-job: observability-release-helm
          build-params: |
            param1=ACME
            param2=BOB
            something=my super duper variable

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `vaultRoleId`     | String  |                             | The Vault role id. |
| `vaultSecretId`   | String  |                             | The Vault secret id. |
| `vaultUrl`        | String  |                             | The Vault URL to connect to. |
| `secret`          | String  | `secret/observability-team/ci/internal-ci-automation` | The Vault secret. |
| `jenkins-url`     | String  |                             | The Jenkins base URL. |
| `jenkins-job`     | String  |                             | The Jenkins job to interact with. |
| `waitFor`         | boolean | `false`                     | Whether to wait for the build to finish. |
| `printBuildLogs`  | boolean | `false`                     | Whether to print the build logs. |
| `build-params`    | String  |                             | Specify the build parameters  in KEY=VALUE format. No double quoting or extra `=` |

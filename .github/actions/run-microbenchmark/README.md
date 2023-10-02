## About

GitHub Action to run the microbenchmark using Vault

___

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration


```yaml
---
name: Run microbenchmark
on:
  workflow_run:
    workflows:
      - ci
    types: [ completed ]

jobs:
  microbenchmark:
    timeout-minutes: 5
    runs-on: ubuntu-latest

    steps:

      - uses: elastic/apm-pipeline-library/.github/actions/run-microbenchmark@current
        with:
          vault-url: ${{ secrets.VAULT_ADDR }}
          vault-role-id: ${{ secrets.VAULT_ROLE_ID }}
          vault-scret-id: ${{ secrets.VAULT_SECRET_ID }}
          script: my-super-script
          environments: |
            FOO=bar
            BRANCH=main

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                                             | Description                                                                                                       |
|-----------------------------|---------|-----------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `vault-role-id`               | String  |                                                     | The Vault role id.                                                                                                |
| `vault-secret-id`             | String  |                                                     | The Vault secret id.                                                                                              |
| `vault-url`                  | String  |                                                     | The Vault URL to connect to.                                                                                      |
| `environments`              | String  |  | Dynamic environment variables with key=value format                                                      |
| `slack-channel`                    | String  |  | The script to run                                                       |
| `script`                    | String  |  | The slack channel to send if any failures.                                                       |

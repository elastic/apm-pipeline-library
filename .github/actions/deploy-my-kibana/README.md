## About

GitHub Action to deploy my kibana process

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Create cluster using the oblt-cli
on:
  issue_comment:
    types: [created]
jobs:
  deploy-my-kibana:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/deploy-my-kibana@current
        with:
          vaultUrl: ${{ secrets.OBLT_VAULT_ADDR }}
          vaultRoleId: ${{ secrets.OBLT_VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.OBLT_VAULT_SECRET_ID }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                         | Description                        |
|-------------------|---------|---------------------------------|------------------------------------|
| `event`           | String  | `${{ github.event }}`           | The GitHub event payload. Json.  |
| `user`            | String  | `${{ github.triggering_actor }}`| The GitHub user avatar           |
| `repository`      | String  | `${{ github.repository }}`      | The GitHub repository, ORG/REPO. |
| `vaultRoleId`     | String  |                                 | The Vault role id. |
| `vaultSecretId`   | String  |                                 | The Vault secret id. |
| `vaultUrl`        | String  |                                 | The Vault URL to connect to. |

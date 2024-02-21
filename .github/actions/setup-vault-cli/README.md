## About

GitHub Action that installs Vault CLI and sets a `VAULT_TOKEN` environment variable.

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: deploy
on:
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      ...

      - uses: elastic/apm-pipeline-library/.github/actions/setup-vault-cli@current
        with:
          url: ${{ secrets.VAULT_ADDR }}
          roleId: ${{ secrets.VAULT_ROLE_ID }}
          secretId: ${{ secrets.VAULT_SECRET_ID }}
      ...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name           | Type    | Default  | Description                         |
|----------------|---------|----------|-------------------------------------|
| `roleId`       | String  |          | The Vault role id.           |
| `secretId`     | String  |          | The Vault secret id.         |
| `url`          | String  |          | The Vault URL to connect to. |

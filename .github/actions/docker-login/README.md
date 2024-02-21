## About

GitHub Action that performs a Docker login using the credentials from a Vault secret.

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

      - uses: elastic/apm-pipeline-library/.github/actions/docker-login@current
        with:
          registry: docker.elastic.co
          secret: secret/observability-team/ci/docker-registry/prod
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
| `registry`     | String |           | Docker registry.                    |
| `secret`       | String  |          | Vault secret with the format {username:"foo", password:"bar"}.  |
| `roleId`       | String  |          | The Vault role id.           |
| `secretId`     | String  |          | The Vault secret id.         |
| `url`          | String  |          | The Vault URL to connect to. |

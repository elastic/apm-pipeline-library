## About

Set up an .npmrc file for npm registry operations.

* [Usage](#usage)
    * [Configuration](#configuration)
* [Customizing](#customizing)
    * [inputs](#inputs)

## Usage

### Configuration

```yaml
---
name: Example

on:
  pull_request: ~

jobs:
  npm:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          cache: npm
          node-version-file: .nvmrc
      - uses: elastic/apm-pipeline-library/.github/actions/setup-npmrc@current
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
      - run: npm whoami
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name            | Type   | Default                                          | Description                                     |
|-----------------|--------|--------------------------------------------------|-------------------------------------------------|
| `vaultRoleId`   | String |                                                  | The Vault role id.                              |
| `vaultSecretId` | String |                                                  | The Vault secret id.                            |
| `vaultUrl`      | String |                                                  | The Vault URL to connect to.                    |
| `secret`        | String | `secret/apm-team/ci/elastic-observability-npmjs` | The Vault secret.                               |
| `secretKey`     | String | `token`                                          | The Vault secret key.                           |
| `registry`      | String | `registry.npmjs.org`                             | NPM Registry.                                   |
| `npmrcFile`     | String | `.npmrc`                                         | Name of the file with the token.                |
| `path`          | String | `.`                                              | Folder where the `.npmrc` file will be created. |

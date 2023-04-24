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
          vault-url: ${{ secrets.VAULT_ADDR }}
          vault-role-id: ${{ secrets.VAULT_ROLE_ID }}
          vault-secret-id: ${{ secrets.VAULT_SECRET_ID }}
      - run: npm whoami
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name            | Type   | Default                                          | Description                                     |
|-----------------|--------|--------------------------------------------------|-------------------------------------------------|
| `vault-role-id`   | String |                                                  | The Vault role id.                              |
| `vault-secret-id` | String |                                                  | The Vault secret id.                            |
| `vault-url`      | String |                                                  | The Vault URL to connect to.                    |
| `secret`        | String | `secret/apm-team/ci/elastic-observability-npmjs` | The Vault secret.                               |
| `secret-key`     | String | `token`                                          | The Vault secret key.                           |
| `registry`      | String | `registry.npmjs.org`                             | NPM Registry.                                   |
| `npmrc-file`     | String | `.npmrc`                                         | Name of the file with the token.                |
| `path`          | String | `.`                                              | Folder where the `.npmrc` file will be created. |

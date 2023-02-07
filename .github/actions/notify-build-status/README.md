## About

Github Action that (currently) sends the build status to Slack, may be extended to email
or any other target.
___

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

```yaml
---
name: example

on:
  push:
    tags:
      - "v*.*.*"
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: exit 0;
      - if: always()
        uses: elastic/apm-pipeline-library/.github/actions/notify-build-status@current
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
          slackChannel: "#some-channel"
      - run: exit 1;
      - if: always()
        uses: elastic/apm-pipeline-library/.github/actions/notify-build-status@current
        with:
          vaultUrl: ${{ secrets.VAULT_ADDR }}
          vaultRoleId: ${{ secrets.VAULT_ROLE_ID }}
          vaultSecretId: ${{ secrets.VAULT_SECRET_ID }}
          slackChannel: "#some-channel"


```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name            | Type    | Required | Description                                             |
|-----------------|---------|------|-------------------------------------------------------------|
| `vaultRoleId`   | String  | yes  | The Vault role id.                                          |
| `vaultSecretId` | String  | yes  | The Vault secret id.                                        |
| `vaultUrl`      | String  | yes  | The Vault URL to connect to.                                |
| `slackChannel`  | String  | no   | Slack channel id, channel name, or user id to post message. |

## About

GitHub Action that sends a Slack message using the credentials from Vault.

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)
  * [outputs](#outputs)

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

      - uses: elastic/apm-pipeline-library/.github/actions/slack-message@current
        id: started
        with:
          url: ${{ secrets.VAULT_ADDR }}
          roleId: ${{ secrets.VAULT_ROLE_ID }}
          secretId: ${{ secrets.VAULT_SECRET_ID }}
          message: "started"
          channel: "#my-channel"
      ...
      - uses: elastic/apm-pipeline-library/.github/actions/slack-message@current
        with:
          url: ${{ secrets.VAULT_ADDR }}
          roleId: ${{ secrets.VAULT_ROLE_ID }}
          secretId: ${{ secrets.VAULT_SECRET_ID }}
          message: "finished"
          channel: "#my-channel"
          threadTimestamp: ${{ steps.started.outputs.threadTimestamp }}
      ...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name           | Type    | Default                              | Description        |
|----------------|---------|--------------------------------------|--------------------|
| `roleId`       | String  |          | The Vault role id.           |
| `secretId`     | String  |          | The Vault secret id.         |
| `url`          | String  |          | The Vault URL to connect to. |
| `channel`      | String  |                                      | Slack channel.     |
| `message`      | String  |                                      | Slack message on Markdown format. Multiline messages must be escaped using URL encoding. |
| `payload`      | String  |                                      | Slack payload. |
| `mask`         | Boolean | `true`                               | Mask the Slack message on the logs.  |
| `urlEncoded`   | Boolean | `true`                               | True if the message is URL encoded. |
| `threadTimestamp`   | Boolean | `true`                          | The timestamp on the message that was posted into Slack when using bot token. |

### outputs

| Name              | Type    | Description               |
|-------------------|---------| --------------------------|
| `threadTimestamp` | String  | The timestamp on the message that was posted into Slack when using bot token. |

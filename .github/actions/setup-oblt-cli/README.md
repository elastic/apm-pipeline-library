## About

GitHub Action to set up oblt-cli

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: List clusters using oblt-cli

on:
  workflow_dispatch:

jobs:
  run-oblt-cli:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/github-token@main
        with:
          url: ${{ secrets.VAULT_ADDR }}
          roleId: ${{ secrets.VAULT_ROLE_ID }}
          secretId: ${{ secrets.VAULT_SECRET_ID }}
      - uses: elastic/apm-pipeline-library/.github/actions/setup-git@current
        with:
          username: ${{ env.GIT_USER }}
          email: ${{ env.GIT_EMAIL }}
          token: ${{ env.GITHUB_TOKEN }}
      - uses: elastic/apm-pipeline-library/.github/actions/setup-oblt-cli@current
        with:
          slack-channel: "#observablt-bots" # default
          username: "obltmachine" # default
          github-token: ${{ env.GITHUB_TOKEN }}

      - run: oblt-cli cluster list
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name            | Type   | Default            | Description                                                          |
| --------------- | ------ | ------------------ | -------------------------------------------------------------------- |
| `slack-channel` | String | `#observablt-bots` | The slack channel to be configured in the oblt-cli.                  |
| `github-token`  | String |                    | The GitHub token with permissions fetch releases.                    |
| `username`      | String | `obltmachine`      | Username to show in the deployments with oblt-cli, format: [a-z0-9]. |
| `version`       | String | `6.5.3`            | Install a specific version of oblt-cli                               |

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

| Name              | Type    | Default                                | Description                        |
|-------------------|---------|----------------------------------------|------------------------------------|
| `issue_url`       | String  | `${{ github.event.comment.issue_url }}`| The GitHub issue URL.  |
| `comment_url`     | String  | `${{ github.event.comment.html_url }}` | The GitHub comment URL.  |
| `comment_id`      | String  | `${{ github.event.comment.id }}`       | The GitHub comment ID.  |
| `user`            | String  | `${{ github.triggering_actor }}`       | The GitHub user avatar           |
| `repository`      | String  | `${{ github.repository }}`             | The GitHub repository, ORG/REPO. |
| `serverless`      | Boolean | `false`                                | Whether to deploy a serverless deployment. |
| `vaultRoleId`     | String  |                                        | The Vault role id. |
| `vaultSecretId`   | String  |                                        | The Vault secret id. |
| `vaultUrl`        | String  |                                        | The Vault URL to connect to. |

### outputs

| Name              | Type    | Description                               |
|-------------------|---------| ------------------------------------------|
| `issue`           | String  | The GitHub issue URL with the deployment. |

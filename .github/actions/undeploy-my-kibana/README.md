## About

GitHub Action to undeploy my kibana process

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Destroy cluster using the oblt-cli
on:
  pull_request:
    types: [closed]
jobs:
  deploy-my-kibana:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/undeploy-my-kibana@current
        with:
          vault-url: ${{ secrets.OBLT_VAULT_ADDR }}
          vault-role-id: ${{ secrets.OBLT_VAULT_ROLE_ID }}
          vault-secret-id: ${{ secrets.OBLT_VAULT_SECRET_ID }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                                | Description                        |
|-------------------|---------|----------------------------------------|------------------------------------|
| `pull-request`    | String  | `${{ github.event.pull_request.number }}`       | The GitHub Pull Request ID.      |
| `user`            | String  | `${{ github.event.pull_request.head.repo.owner.login }}`       | The GitHub user avatar           |
| `repository`      | String  | `${{ github.repository }}`             | The GitHub repository, ORG/REPO. |
| `vault-role-id`   | String |                                         | The Vault role id.               |
| `vault-secret-id` | String |                                         | The Vault secret id.             |
| `vault-url`       | String |                                         | The Vault URL to connect to.     |

### outputs

| Name              | Type    | Description                               |
|-------------------|---------| ------------------------------------------|
| `issue`           | String  | The GitHub issue URL with the deployment. |

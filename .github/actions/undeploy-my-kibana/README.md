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
  undeploy-my-kibana:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/undeploy-my-kibana@current
        with:
          token: ${{ secrets.MY_SUPER_TOKEN }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                                                 | Description                        |
|-------------------|---------|---------------------------------------------------------|------------------------------------|
| `pull-request`    | String  | `${{ github.event.pull_request.number }}`               | The GitHub Pull Request ID.      |
| `user`            | String  | `${{ github.event.pull_request.head.repo.owner.login }}`| The GitHub user avatar           |
| `repository`      | String  | `${{ github.repository }}`                              | The GitHub repository, ORG/REPO. |
| `token`           | String  |                                                         | GitHub token.      |

### outputs

| Name              | Type    | Description                               |
|-------------------|---------| ------------------------------------------|
| `issue`           | String  | The GitHub issue URL with the deployment. |

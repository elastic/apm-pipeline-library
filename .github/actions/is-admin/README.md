## About

GitHub Action to check if someone is admin of the given GitHub repository.

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
name: Is Admin Example
on:
  issues:
    types: [opened]
jobs:
  run-if-admin:
    runs-on: ubuntu-latest
    steps:
      - id: is_admin
        uses: elastic/apm-pipeline-library/.github/actions/is-admin@current
        with:
          username: ${{ github.event.issue.user.login }}
      - if: steps.is_admin.outputs.result == true
        run: echo '${{ github.event.issue.user.login }} is admin'
```


## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `username`        | String  |                             | The GitHub user name |
| `repository`      | String  | `github.repository`         | The GitHub repository, format: ORG/REPO |
| `token`           | String  |                             | The GitHub token                   |

### outputs

| Name              | Type    | Description                 |
|-------------------|---------| ----------------------------|
| `result`          | Boolean | Whether the user is admin.  |

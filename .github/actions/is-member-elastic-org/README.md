## About

GitHub Action to check if someone is a member of the GitHub organization.

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
name: Is Member Example
on:
  issues:
    types: [opened]
jobs:
  run-if-member:
    name: Welcome
    runs-on: ubuntu-latest
    steps:
      - id: is_elastic_member
        uses: elastic/apm-pipeline-library/.github/actions/is-member-elastic-org@current
        with:
          username: ${{ github.event.issue.user.login }}
          token: ${{ secrets.PAT_TOKEN }}
      - if: contains(steps.is_elastic_member.outputs.result, 'true')
        run: echo '${{ github.event.issue.user.login }} is member'
      - if: contains(steps.is_elastic_member.outputs.result, 'false')
        run: echo '${{ github.event.issue.user.login }} is not a member'
```


## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name         | Type    | Default           | Description          |
|--------------|---------|-------------------|----------------------|
| `username`   | String  |                   | The GitHub user name |

### outputs

| Name              | Type                       | Description                 |
|-------------------|----------------------------| ----------------------------|
| `result`          | String (`true` or `false`) | Whether the user is member. |

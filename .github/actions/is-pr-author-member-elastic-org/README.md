## About

GitHub Action to check if the PR author is member of the GitHub organization.

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
name: Is PR Author member Example
on:
  issues:
    types: [opened]
jobs:
  run-if-member:
    name: Welcome
    runs-on: ubuntu-latest
    steps:
      - id: is_pr_author_elastic_member
        uses: elastic/apm-pipeline-library/.github/actions/is-pr-author-member-elastic-org@current
        with:
          pull-request: ${{ github.event.issue.number }}
          repository: ${{ github.repository }}
          token: ${{ secrets.PAT_TOKEN }}
      - if: steps.is_pr_author_elastic_member.outputs.result == true
        run: echo 'PR author of ${{ github.event.issue.number }} is Elastic member'
```


## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `pull-request`    | String  |                             | The GitHub Pull Request            |
| `repository`      | String  |                             | The GitHub repository, format: ORG/REPO |
| `token`           | String  |                             | The GitHub token                   |

### outputs

| Name              | Type    | Description                 |
|-------------------|---------| ----------------------------|
| `result`          | Boolean | Whether the PR author is member. |

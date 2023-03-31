## About

GitHub Action to validate whether the GitHub comment was triggered by a user with write permissions. Otherwise, it will report a message.

* [Usage](#usage)
  * [Configuration](#configuration)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Is GitHub comment allowed
on:
  issue_comment:
    types: [created]
jobs:
  run-action-if-comment:
    if: github.event.issue.pull_request && startsWith(github.event.comment.body, '/run-test')
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/validate-github-comment@current
      ...
```

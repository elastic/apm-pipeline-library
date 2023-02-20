## About

GitHub Action to react to the given comment with an emoji (default +1).

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)
  * [outputs](#outputs)

## Usage

### Configuration

```yaml
---
name: React to comment in a Pull Request
on:
  issue_comment:
    types:
      - created
jobs:
  react:
    runs-on: ubuntu-latest
    if: ${{ github.event.issue.pull_request }}
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/comment-reaction@current
        with:
          repo: ${{ github.event.issue.user.login }}
          commentId: ${{ github.event.issue.user.login }}
          emoji: hooray
          token: ${{ secrets.PAT_TOKEN }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `commentId`       | String  |                             | The GitHub comment Id              |
| `repo`            | String  |                             | The GitHub repository name         |
| `emoji`           | String  | `+1`                        | The emoji reaction, see https://docs.github.com/en/rest/reactions?apiVersion=2022-11-28#about-reactions |
| `token`           | String  |                             | The GitHub token                   |

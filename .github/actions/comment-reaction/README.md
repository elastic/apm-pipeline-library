## About

GitHub Action to react to the given comment with an emoji (default +1).

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)
  * [outputs](#outputs)

## Usage

### Configuration

Given the `PAT_TOKEN` GitHub secret then react to a comment in a Pull Request.

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
          repository: ${{ github.repository }}
          commentId: ${{ github.event.comment.id }}
          emoji: hooray
          token: ${{ secrets.PAT_TOKEN }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `commentId`       | String  |                             | The GitHub comment Id              |
| `repository`      | String  |                             | The GitHub repository, format: ORG/REPO |
| `emoji`           | String  | `+1`                        | The emoji reaction, see https://docs.github.com/en/rest/reactions?apiVersion=2022-11-28#about-reactions |
| `token`           | String  |                             | The GitHub token                   |

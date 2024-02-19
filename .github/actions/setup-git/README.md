## About

GitHub Action that setups the git username, email, and authentication with git CLI

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: deploy
on:
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      ...

      - uses: elastic/apm-pipeline-library/.github/actions/setup-git@current
        with:
          username: "John"
          email: "john@acme.com"
          token: ${{ secrets.MY_GITHUB_PAT }}
      ...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name           | Type    | Default                              | Description        |
|----------------|---------|--------------------------------------|--------------------|
| `username`     | String  | `apmmachine`                         | Git username       |
| `secretId`     | String  | `apmmachine@users.noreply.github.com`| Git email.         |
| `trace`        | Boolean | `false`                              | Enable git trace.  |
| `token`        | String  | `github.token`                       | GitHub token.      |

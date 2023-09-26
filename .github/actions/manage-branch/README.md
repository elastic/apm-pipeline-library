## About

GitHub Action to manage the given GitHub branches

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Lock Branch

jobs:
  lock-main-branch:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/manage-branch@current
        with:
          branch: 'main'
          lock: true
          owner: elastic
          repository: apm-agent-java
          token: ${{ secrets.PAT }}
```


## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                     | Description                        |
|-------------------|---------|-----------------------------|------------------------------------|
| `branch`          | String  |                             | The GitHub branch name             |
| `lock`            | Boolean |                             | Whether to lock the given branch   |
| `owner`           | String  | `github.repository.owner`   | The GitHub owner                   |
| `repo`            | String  | `github.repository.repo`    | The GitHub repo                    |
| `token`           | String  |                             | The GitHub token                   |

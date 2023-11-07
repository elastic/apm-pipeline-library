## About

GitHub Action to run the oblt-cli wrapper to destroy any given cluster

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
name: Create the cluster using oblt-cli and destroy it

...

jobs:
  create-cluster-and-destroy:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-create-ccs@current
        with:
          remote-cluster: 'dev-oblt'
          cluster-name-prefix: 'foo'
          cluster-name-sufix: 'bar'
          token: ${{ secrets.PAT_TOKEN }}

      ...

      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-destroy-cluster@current
        with:
          cluster-name: 'foo'
          github-token: ${{ secrets.PAT_TOKEN }}
        if: always()
...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                     | Description                                       |
|-----------------------------|---------|-----------------------------|-------------------------------------------------- |
| `cluster-name `             | String  | Optional                    | The cluster name                                  |
| `cluster-info-file `        | String  | Optional                    | The cluster info file (absolute path)             |
| `github-token`              | String  | Mandatory                   | The GitHub token with permissions fetch releases. |

### outputs

Unmasked environment variable:

* CLUSTER_NAME

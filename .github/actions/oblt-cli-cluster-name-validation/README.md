## About

GitHub Action to gather the cluster-name and run some validation based on the different inputs.

This is likely to be used within other GitHub actions.

* [Usage](#usage)
  * [Configuration](#configuration)
* [Customizing](#customizing)
  * [inputs](#inputs)
  * [output](#output)

## Usage

### Configuration

Given the CI GitHub action:

```yaml
---
name: Get cluster name

...

jobs:
  get-cluster-name:
    runs-on: ubuntu-latest
    steps:
      - id: cluster
        uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-cluster-name-validation@current
        with:
          cluster-name: 'edge-oblt'
      ...
      - run: echo "${{ steps.cluster.outputs.cluster-name }}

...
```

or alternatively if you use `oblt-cli` with `--output-file "${CLUSTER_INFO_FILE}"'` then

```yaml
---
name: Get cluster name given the cluster info file

...

jobs:
  cat-indices:
    runs-on: ubuntu-latest
    steps:
      ...
      - uses: elastic/apm-pipeline-library/.github/actions/oblt-cli@current
        with:
          command: 'cluster create ... --output-file "${{ github.workspace }}/cluster-info.json" --wait 15'

      - id: cluster
        uses: elastic/apm-pipeline-library/.github/actions/oblt-cli-cluster-name-validation@current
        with:
          cluster-info-file: ${{ github.workspace }}/cluster-info.json
      ...
      - run: echo "${{ steps.cluster.outputs.cluster-name }}

...
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name                        | Type    | Default                     | Description                                       |
|-----------------------------|---------|-----------------------------|-------------------------------------------------- |
| `cluster-name`              | String  | Optional                    | The cluster name                                  |
| `cluster-info-file`         | String  | Optional                    | The cluster info file (absolute path)             |

### outputs

| Name           | Type    | Description       |
|----------------|---------|-------------------|
| `cluster-name` | String  | The cluster name. |

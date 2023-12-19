
## About

GitHub Action to fetch the current list of active branches in Elastic (the ones based
on the Unified Release process)

___

* [Usage](#usage)
  * [Configuration](#configuration)

## Usage

### Configuration

```yaml
---
...

jobs:
  filter:
    runs-on: ubuntu-latest
    timeout-minutes: 1
    outputs:
      matrix: ${{ steps.generator.outputs.matrix }}
    steps:
      - id: generator
        uses: elastic/apm-pipeline-library/.github/actions/elastic-stack-snapshot-branches@current
        with:
          exclude-branches: '7.17'

  bump-elastic-stack:
    runs-on: ubuntu-latest
    needs: [filter]
    strategy:
      matrix: ${{ fromJson(needs.filter.outputs.matrix) }}
...

```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name              | Type    | Default                                | Description                        |
|-------------------|---------|----------------------------------------|------------------------------------|
| `exclude-branches`| List strings(comma-split) | | The list of exlcuded branches (comma separated).  |

### outputs

| Name              | Type    | Description                               |
|-------------------|---------| ------------------------------------------|
| `matrix`          | matrix  | The matrix in the format include.         |
| `branches`        | list    | The list of branches.                     |

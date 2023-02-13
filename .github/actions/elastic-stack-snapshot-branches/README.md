
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

  bump-elastic-stack:
    runs-on: ubuntu-latest
    needs: [filter]
    strategy:
      matrix: ${{ fromJson(needs.filter.outputs.matrix) }}
...

```

## About

GitHub Action that exits with the combined status of the provided needs context.
This is useful for creating a single status check.

That status check can the be set as required status check or it can be used
in combination with the `notify-built-status` action.

* [Usage](#usage)
    * [Configuration](#configuration)
* [Customizing](#customizing)
    * [inputs](#inputs)

## Usage

### Configuration

```yaml
---
name: Example

on:
  pull_request: ~

jobs:

  job-a:
    runs-on: ubuntu-latest
    steps:
      - run: exit 1;
  job-b:
    runs-on: ubuntu-latest
    steps:
      - run: exit 0;

  job-c:
    if: always()
    runs-on: ubuntu-latest
    needs:
      - job-a
      - job-b
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/combine-needs-status@current
        with: ${{ toJSON(needs) }}
```

## Customizing

### inputs

Following inputs can be used as `step.with` keys

| Name         | Type    | Default                     | Description                      |
|--------------|---------|-----------------------------|----------------------------------|
| `needs`      | String  |                             | JSON string of the needs context |

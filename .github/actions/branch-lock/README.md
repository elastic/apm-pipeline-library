# branch-lock
[![License: Apache 2.0](https://img.shields.io/badge/license-Apache--2.0-yellow)](https://opensource.org/license/apache-2-0/)

*Lock/Unlock a branch using branch protections.*
* [Source](https://github.com/elastic/apm-pipeline-library)
* [Issues](https://github.com/elastic/apm-pipeline-library/issues)
* [Contact](mailto:observability-robots@elastic.co)

## Prerequisites
* [NodeJS](https://nodejs.org/en) for development.

## Workflow

### Setup
The following steps will ensure your project is cloned properly.
1. `npm i`

### Lint
* To lint you have to use the workflow.

```bash
npm run lint
```

* It will lint the project code using `eslint`.

### Format
* To format you have to use the workflow.

```bash
npm run fmt
```

* It will format the project code using `prettier`.

### Build
* To build you have to use the workflow.

```bash
npm run build
```

* It will test the project code using `tsc`.

### Test
* To test you have to use the workflow.

```bash
npm test
```

* It will test the project code using `jest`.

### Package
* To test you have to use the workflow.

```bash
npm run package
```

* It will package the project code using `ncc`.

## Usage

### How to use it

```yaml
---
name: Branch Lock/Unlock

on:
  pull_request:
  push:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  branch-lock:
    runs-on: ubuntu-latest
    steps:
      - uses: elastic/apm-pipeline-library/.github/actions/branch-lock@current
        with:
          # Define a specific branch (required)
          branch: 'main'
          # Define if you want to lock or unlock (required)
          lock: 'true'
          # Define a specific GitHub token (required)
          token: ${{ secrets.PAT }}
          # Define a specific org (optional)
          owner: ''
          # Define a specific repo (optional)
          repo: ''
```

## Contributing

If you find this project useful here's how you can help :

* Send a Pull Request with your awesome new features and bug fixed
* Be a part of the community and help resolve [Issues](https://github.com/elastic/apm-pipeline-library/issues)

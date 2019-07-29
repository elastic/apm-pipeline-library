#!/usr/bin/env bash
set -eo pipefail
## Further details: https://github.com/elastic/infra/blob/master/flavortown/jjbb/README.md#how-do-i-test-changes-locally

VAULT_TOKEN=$(cat ~/.vault-token) \
docker run --rm -e VAULT_TOKEN -e HOME=/jjbb \
        -v "$(pwd)":/jjbb -w /jjbb \
        --network=host docker.elastic.co/infra/jjbb \
        --dry-run \
        --cluster=apm-ci

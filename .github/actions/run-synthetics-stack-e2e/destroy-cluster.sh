#!/usr/bin/env bash

set -euo pipefail

cluster_name=${1:?}

oblt-cli cluster destroy \
      --force \
      --disable-banner \
      --cluster-name="${cluster_name}"

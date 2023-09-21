#!/usr/bin/env bash

set -euo pipefail

cluster_state_output_file="${RUNNER_TEMP}/cluster-state.yml"

cluster_name=${1:?}

oblt-cli cluster secrets cluster-state \
    --disable-banner \
    --cluster-name "${cluster_name}" \
    --output-file "${cluster_state_output_file}"

kibana_url=$(yq '.kibana_url' "${cluster_state_output_file}")
kibana_username=$(yq '.kibana_username' "${cluster_state_output_file}")
kibana_password=$(yq '.kibana_password' "${cluster_state_output_file}")

echo "::add-mask::${kibana_url}"
# Don't mask the username for now, because this is usually "elastic" has a lot of
# occurrences in the logs.
# echo "::add-mask::${kibana_username}"
echo "::add-mask::${kibana_password}"

{
  echo "cluster-name=${cluster_name}"
  echo "kibana-url=${kibana_url}"
  echo "kibana-username=${kibana_username}"
  echo "kibana-password=${kibana_password}"
} >> "${GITHUB_OUTPUT}"

rm -f "${cluster_state_output_file}"

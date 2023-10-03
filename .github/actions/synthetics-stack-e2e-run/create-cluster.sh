#!/usr/bin/env bash

set -euo pipefail

kibana_docker_image=${1:?}
stack_version=${2:?}

create_output_file="${RUNNER_TEMP}/cluster.json"

oblt-cli cluster create custom \
  --template="ess" \
  --cluster-name-prefix="synthetics-stack-e2e" \
  --parameters="{\"StackVersion\":\"${stack_version}\", \"KibanaDockerImage\": \"${kibana_docker_image}\", \"EphemeralCluster\": true}" \
  --wait 15 \
  --disable-banner \
  --verbose \
  --output-file="${create_output_file}"

cluster_name=$(jq -r '.ClusterName' < "${create_output_file}")

{
  echo "cluster-name=${cluster_name}"
} >> "${GITHUB_OUTPUT}"

rm -f "${create_output_file}"

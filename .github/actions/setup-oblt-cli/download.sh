#!/usr/bin/env bash

set -euo pipefail

BIN_DIR="$HOME/oblt-cli/bin"

mkdir -p "${BIN_DIR}"

if [[ ${RUNNER_OS} == "Linux" ]]; then
  if [[ ${RUNNER_ARCH} == "X64" ]]; then
    gh release download --repo elastic/observability-test-environments -p '*linux_amd64.tar.gz' --output - | tar -xz -C "${BIN_DIR}"
  elif [[ ${RUNNER_ARCH} == "ARM64" ]]; then
    gh release download --repo elastic/observability-test-environments -p '*linux_arm64.tar.gz' --output - | tar -xz -C "${BIN_DIR}"
  else
    echo "Unsupported architecture for ${RUNNER_OS}: ${RUNNER_ARCH}"
    exit 1
  fi
elif [[ ${RUNNER_OS} == "macOS" ]]; then
  if [[ ${RUNNER_ARCH} == "X64" ]]; then
      gh release download --repo elastic/observability-test-environments -p '*darwin_amd64.tar.gz' --output - | tar -xz -C "${BIN_DIR}"
    elif [[ ${RUNNER_ARCH} == "ARM64" ]]; then
      gh release download --repo elastic/observability-test-environments -p '*darwin_arm64.tar.gz' --output - | tar -xz -C "${BIN_DIR}"
    else
      echo "Unsupported architecture for ${RUNNER_OS}: ${RUNNER_ARCH}"
      exit 1
    fi
else
  echo "Unsupported OS: ${RUNNER_OS}"
  exit 1
fi

echo "${BIN_DIR}" >> "${GITHUB_PATH}"

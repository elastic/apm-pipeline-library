#!/usr/bin/env bash

set -euo pipefail

if [[ ${RUNNER_OS} == "Linux" ]]; then
  if [[ ${RUNNER_ARCH} == "X64" ]]; then
    gh release download --repo elastic/observability-test-environments -p '*linux_amd64.tar.gz' --output - | tar -xz -C "$HOME"
  elif [[ ${RUNNER_ARCH} == "ARM64" ]]; then
    gh release download --repo elastic/observability-test-environments -p '*linux_arm64.tar.gz' --output - | tar -xz -C "$HOME"
  else
    echo "Unsupported architecture for ${RUNNER_OS}: ${RUNNER_ARCH}"
    exit 1
  fi
elif [[ ${RUNNER_OS} == "macOS" ]]; then
  if [[ ${RUNNER_ARCH} == "X64" ]]; then
      gh release download --repo elastic/observability-test-environments -p '*darwin_amd64.tar.gz' --output - | tar -xz -C "$HOME"
    elif [[ ${RUNNER_ARCH} == "ARM64" ]]; then
      gh release download --repo elastic/observability-test-environments -p '*darwin_arm64.tar.gz' --output - | tar -xz -C "$HOME"
    else
      echo "Unsupported architecture for ${RUNNER_OS}: ${RUNNER_ARCH}"
      exit 1
    fi
else
  echo "Unsupported OS: ${RUNNER_OS}"
  exit 1
fi

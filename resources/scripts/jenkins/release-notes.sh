#!/usr/bin/env bash
set -uxeo pipefail

gren release --override -c .grenrc.js -t all
# it is generated from scratch to have reverse version order
gren changelog --override -c .grenrc.js -t all -G

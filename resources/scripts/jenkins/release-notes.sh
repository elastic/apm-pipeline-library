#!/usr/bin/env bash
set -uxeo pipefail

gren release --username=elastic --override -c .grenrc.js -t all
# it is generated from scratch to have reverse version order
gren changelog --username=elastic --override -c .grenrc.js -t all -G

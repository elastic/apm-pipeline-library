#!/usr/bin/env bash
set -eo pipefail

IMAGE="gherkin/lint"
docker pull "${IMAGE}" > /dev/null || true

arguments=""
declare -a files

set +e

while [ "$1" != "" ]; do
    case $1 in
        --disable )
            arguments="$arguments $1 $2"
            shift
            ;;
        --enable )
            arguments="$arguments $1 $2"
            shift
            ;;
        -v | --verbose )
            arguments="$arguments $1"
            ;;
        * )
            files+=("$1")
    esac
    shift
done

echo "Running gherkin-lint with arguments: '${arguments}'"

## Iterate for each file without failing fast.
for file in "${files[@]}"; do
  # shellcheck disable=SC2086
  if ! docker run --rm -t -v "$(pwd)":/src -w /src "${IMAGE}" ${arguments} ${file}; then
    echo "ERROR: gherkin-lint failed for the file '${file}'. Arguments: ${arguments}"
    exit_status=1
  fi
done

exit $exit_status

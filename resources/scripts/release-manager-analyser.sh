#!/usr/bin/env bash
#
# This script is executed after the release snapshot stage.
# to help with the debugging/troubleshooting
#
set -uo pipefail
set +e
RAW_OUTPUT=${RAW_OUTPUT:?'Missing the release manager output file'}
REPORT=${REPORT:?'Missing the release manager report file'}

if [ -f "$RAW_OUTPUT" ] ; then
    echo "There are some errors, let's guess what they are about:" > "$REPORT"
    if grep 'Vault responded with HTTP status code' "$RAW_OUTPUT" ; then
        echo 'Environmental issue with Vault. Try again' >> "$REPORT"
    fi
    if grep 'Cannot write to file' "$RAW_OUTPUT" ; then
        echo 'Artifacts were not generated. Likely a genuine issue' >> "$REPORT"
    fi
    if grep 'does not exist' "$RAW_OUTPUT" ; then
        echo 'Build file does not exist in the unified release. Likely the branch is not supported yet. Contact the release platform team' >> "$REPORT"
    fi
else
    echo "WARN: $RAW_OUTPUT does not exist"
fi

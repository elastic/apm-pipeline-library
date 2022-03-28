#!/usr/bin/env bash
#
# This script is executed after the release snapshot stage.
# to help with the debugging/troubleshooting
#
set -uxo pipefail
RAW_OUTPUT=${RAW_OUTPUT:?'Missing the release manager output file'}
REPORT=${REPORT:?'Missing the release manager report file'}

if [ -f "$RAW_OUTPUT" ] ; then
    echo "There were some errors while running the release manager, let's analyse them." > "$REPORT"
    if grep -q -i 'Vault responded with HTTP status code' "$RAW_OUTPUT" ; then
        echo '* Environmental issue with Vault. Try again' >> "$REPORT"
    fi
    if grep -q -i 'Cannot write to file' "$RAW_OUTPUT" ; then
        echo '* Artifacts were not generated. Likely a genuine issue' >> "$REPORT"
    fi
    if grep -q -i 'does not exist' "$RAW_OUTPUT" ; then
        echo '* Build file does not exist in the unified release. Likely the branch is not supported yet. Contact the release platform team' >> "$REPORT"
    fi
else
    echo "WARN: $RAW_OUTPUT does not exist"
fi

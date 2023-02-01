#!/usr/bin/env bash
grep -v -q "$1" vars/goDefaultVersion.groovy

exit $?

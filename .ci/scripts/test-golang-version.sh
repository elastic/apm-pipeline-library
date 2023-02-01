#!/usr/bin/env bash
grep -v -q vars/goDefaultVersion.groovy "$1"

exit $?

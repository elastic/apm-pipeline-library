# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

import pytest
from utils import assertTest, STATUS_CODE_ERROR

pytest_plugins = ["pytester"]

common_code = """
import os
import time
import logging
import pytest

"""


def test_failure_code_plugin(pytester, otel_service):
    """test a test with a code exception"""
    pytester.makepyfile(
        common_code
        + """
def test_failure_code():
    d = 1/0
    pass
""")
    assertTest(pytester, "test_failure_code", "failed", STATUS_CODE_ERROR, "failed", STATUS_CODE_ERROR)

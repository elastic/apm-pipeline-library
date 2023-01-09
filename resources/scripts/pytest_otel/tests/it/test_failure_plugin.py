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


def test_failure_plugin(pytester, otel_service):
    """test a failed test"""
    pytester.makepyfile(
        common_code
        + """
def test_failure():
    assert 1 < 0
""")
    assertTest(pytester, "test_failure", "failed", STATUS_CODE_ERROR, "failed", STATUS_CODE_ERROR)

# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

import pytest
from utils import assertTest

pytest_plugins = ["pytester"]

common_code = """
import os
import time
import logging
import pytest

"""


def test_basic_plugin(pytester, otel_service):
    """test a simple test"""
    pytester.makepyfile(
        common_code
        + """
def test_basic():
    time.sleep(5)
    pass
""")
    assertTest(pytester, "test_basic", "passed", "STATUS_CODE_OK", "passed", "STATUS_CODE_OK")

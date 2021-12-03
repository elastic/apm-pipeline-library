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


def test_success_plugin(pytester, otel_service):
    """test a success test"""
    pytester.makepyfile(
        common_code
        + """
def test_success():
    assert True
""")
    assertTest(pytester, "test_success", "passed", "STATUS_CODE_OK", "passed", "STATUS_CODE_OK")

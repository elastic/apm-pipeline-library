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


def test_skip_plugin(pytester, otel_service):
    """test a skipped test"""
    pytester.makepyfile(
        common_code
        + """
@pytest.mark.skip
def test_skip():
    assert True
""")
    assertTest(pytester, None, "passed", "STATUS_CODE_OK", None, None)

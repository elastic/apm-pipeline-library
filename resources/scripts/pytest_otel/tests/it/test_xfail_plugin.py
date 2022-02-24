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


def test_xfail_plugin(pytester, otel_service):
    """test a marked as xfail test"""
    pytester.makepyfile(
        common_code
        + """
@pytest.mark.xfail(reason="foo bug")
def test_xfail():
    assert False
""")
    assertTest(pytester, None, "passed", "STATUS_CODE_OK", None, None)

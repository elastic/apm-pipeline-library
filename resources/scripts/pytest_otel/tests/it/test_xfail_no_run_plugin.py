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


def test_xfail_no_run_plugin(pytester, otel_service):
    """test a marked as xfail test with run==false"""
    pytester.makepyfile(
        common_code
        + """
@pytest.mark.xfail(run=False)
def test_xfail_no_run():
    assert False
""")
    assertTest(pytester, None, "passed", "STATUS_CODE_OK", None, None)

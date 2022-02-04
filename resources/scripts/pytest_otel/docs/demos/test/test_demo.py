# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

pytest_plugins = ["pytester"]

import time
import logging
import pytest


def test_basic():
    time.sleep(5)
    pass

def test_success():
    assert True

def test_failure():
    assert 1 < 0

def test_failure_code():
    d = 1/0
    pass

@pytest.mark.skip
def test_skip():
    assert True

@pytest.mark.xfail(reason="foo bug")
def test_xfail():
    assert False

@pytest.mark.xfail(run=False)
def test_xfail_no_run():
    assert False

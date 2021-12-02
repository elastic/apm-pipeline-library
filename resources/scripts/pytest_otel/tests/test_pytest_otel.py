# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

import pytest
import json

pytest_plugins = ["pytester"]

common_code = """
import time
import logging
import pytest

"""


def assertTestSuit(span, outcome, status):
    assert span["kind"] == "SpanKind.SERVER"
    assert span["status"]["status_code"] == status
    "OK" or span["status"]["status_code"] == "UNSET"
    if outcome is not None:
        assert span["attributes"]["test.outcome"] == "passed"
    assert span["parent_id"] is None
    return True


def assertSpan(span, name, outcome, status):
    assert span["kind"] == "SpanKind.INTERNAL"
    assert span["status"]["status_code"] == status
    assert span["attributes"]["test.name"] == name
    if outcome is not None:
        assert span["attributes"]["test.outcome"] == outcome
    assert len(span["parent_id"]) > 0
    return True


def assertTest(pytester, name, ts_outcome, ts_status, outcome, status):
    pytester.runpytest("--otel-span-file-output=./test_spans.json")
    span_list = None
    with open("test_spans.json", encoding='utf-8') as input:
        span_list = json.loads(input.read())
    foundTest = False
    foundTestSuit = False
    for span in span_list:
        if span["name"] == "Running {}".format(name):
            foundTest = assertSpan(span, name, outcome, status)
        if span["name"] == "Test Suite":
            foundTestSuit = assertTestSuit(span, ts_outcome, ts_status)
    assert foundTest or name is None
    assert foundTestSuit


def test_basic_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
def test_basic():
    time.sleep(5)
    pass
""")
    assertTest(pytester, "test_basic", "passed", "OK", "passed", "OK")


def test_success_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
def test_success():
    assert True
""")
    assertTest(pytester, "test_success", "passed", "OK", "passed", "OK")


def test_failure_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
def test_failure():
    assert 1 < 0
""")
    assertTest(pytester, "test_failure", "failed", "OK", "failed", "ERROR")


def test_failure_code_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
def test_failure_code():
    d = 1/0
    pass
""")
    assertTest(pytester, "test_failure_code", "failed", "OK", "failed", "ERROR")


def test_skip_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
@pytest.mark.skip
def test_skip():
    assert True
""")
    assertTest(pytester, None, "passed", "OK", None, None)


def test_xfail_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
@pytest.mark.xfail
def test_xfail():
    assert False
""")
    assertTest(pytester, None, "passed", "OK", None, None)


def test_xfail_no_run_plugin(pytester):
    pytester.makepyfile(
        common_code
        + """
@pytest.mark.xfail(run=False)
def test_xfail_no_run():
    assert False
""")
    assertTest(pytester, None, "passed", "OK", None, None)

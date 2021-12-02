# Copyright The OpenTelemetry Authors
# SPDX-License-Identifier: Apache-2.0

import pytest
import json
import time
import os

pytest_plugins = ["pytester"]

common_code = """
import os
import time
import logging
import pytest

"""


def getSize(filename):
    """return the size of a file"""
    if os.path.isfile(filename):
        st = os.stat(filename)
        return st.st_size


def waitForFileContent(filename):
    """wait for a file has content"""
    while getSize(filename) < 1:
        time.sleep(5)


def assertAttrKeyValue(attributes, key, value):
    """check the value of a key in attributes"""
    realValue = ''
    for attr in attributes:
        if attr['key'] == key:
            realValue = attr["value"]["stringValue"]
    assert realValue == value


def assertTestSuit(span, outcome, status):
    """check attributes of a test suit span"""
    assert span["kind"] == "SPAN_KIND_SERVER"
    assert span["status"]["code"] == status
    if outcome is not None:
        assertAttrKeyValue(span["attributes"], 'test.outcome', outcome)
    assert len(span["parentSpanId"]) == 0
    return True


def assertSpan(span, name, outcome, status):
    """check attributes of a span"""
    assert span["kind"] == "SPAN_KIND_INTERNAL"
    assert span["status"]["code"] == status
    assertAttrKeyValue(span["attributes"], 'test.name', name)
    if outcome is not None:
        assertAttrKeyValue(span["attributes"], 'test.outcome', outcome)
    assert len(span["parentSpanId"]) > 0
    return True


def assertTest(pytester, name, ts_outcome, ts_status, outcome, status):
    """check a test results are correct"""
    ret = pytester.runpytest("--endpoint=http://127.0.0.1:4317", "--service-name=pytest_otel")
    print(ret.outlines)
    print(ret.stderr)
    span_list = None
    filename = "/Users/inifc/src/apm-pipeline-library/resources/scripts/pytest_otel/temp/tests.json"
    waitForFileContent(filename)
    with open(filename, encoding='utf-8') as input:
        spans_output = json.loads(input.readline())
    foundTest = False
    foundTestSuit = False
    print("""
        spans_output {}
        resourceSpans {}
    """.format(
        spans_output,
        spans_output['resourceSpans'],
    ))
    for resourceSpan in spans_output['resourceSpans']:
        for instrumentationLibrarySpan in resourceSpan['instrumentationLibrarySpans']:
            for span in instrumentationLibrarySpan['spans']:
                if span["name"] == "Running {}".format(name):
                    foundTest = assertSpan(span, name, outcome, status)
                if span["name"] == "Test Suite":
                    foundTestSuit = assertTestSuit(span, ts_outcome, ts_status)
    assert foundTest or name is None
    assert foundTestSuit


def test_basic_plugin(pytester):
    """test a simple test"""
    pytester.makepyfile(
        common_code
        + """
def test_basic():
    time.sleep(5)
    pass
""")
    assertTest(pytester, "test_basic", "passed", "STATUS_CODE_OK", "passed", "STATUS_CODE_OK")

#
# def test_success_plugin(pytester):
#     """test a success test"""
#     pytester.makepyfile(
#         common_code
#         + """
# def test_success():
#     assert True
# """)
#     assertTest(pytester, "test_success", "passed", "STATUS_CODE_OK", "passed", "STATUS_CODE_OK")


# def test_failure_plugin(pytester):
#     """test a failed test"""
#     pytester.makepyfile(
#         common_code
#         + """
# def test_failure():
#     assert 1 < 0
# """)
#     assertTest(pytester, "test_failure", "failed", "STATUS_CODE_OK", "failed", "STATUS_CODE_ERROR")
#
#
# def test_failure_code_plugin(pytester):
#     """test a test with a code exception"""
#     pytester.makepyfile(
#         common_code
#         + """
# def test_failure_code():
#     d = 1/0
#     pass
# """)
#     assertTest(pytester, "test_failure_code", "failed", "STATUS_CODE_OK", "failed", "STATUS_CODE_ERROR")
#
#
# def test_skip_plugin(pytester):
#     """test a skipped test"""
#     pytester.makepyfile(
#         common_code
#         + """
# @pytest.mark.skip
# def test_skip():
#     assert True
# """)
#     assertTest(pytester, None, "passed", "STATUS_CODE_OK", None, None)
#
#
# def test_xfail_plugin(pytester):
#     """test a marked as xfail test"""
#     pytester.makepyfile(
#         common_code
#         + """
# @pytest.mark.xfail
# def test_xfail():
#     assert False
# """)
#     assertTest(pytester, None, "passed", "STATUS_CODE_OK", None, None)
#
#
# def test_xfail_no_run_plugin(pytester):
#     """test a marked as xfail test with run==false"""
#     pytester.makepyfile(
#         common_code
#         + """
# @pytest.mark.xfail(run=False)
# def test_xfail_no_run():
#     assert False
# """)
#     assertTest(pytester, None, "passed", "STATUS_CODE_OK", None, None)

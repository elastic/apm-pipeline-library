# Licensed to Elasticsearch B.V. under one or more contributor
# license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright
# ownership. Elasticsearch B.V. licenses this file to you under
# the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import pytest
import json

pytest_plugins = ["pytester"]

common_code = """
import time
import logging

"""


@pytest.fixture
def testdir(testdir):
    if hasattr(testdir, "runpytest_subprocess"):
        # on pytest-2.8 runpytest runs inline by default
        # patch the testdir instance to use the subprocess method
        testdir.runpytest = testdir.runpytest_subprocess
    return testdir


def test_basic_wrapper(testdir):
    testdir.makepyfile(
        common_code
        + """
def test_basic(caplog):
    time.sleep(5)
    pass
"""
    )
    testdir.runpytest("--otel-span-file-output=./test_spans.json")
    span_list = None
    with open("test_spans.json", encoding='utf-8') as input:
        span_list = json.loads(input.read())
    foundTestBasic = False
    foundTestSuit = False
    for span in span_list:
        if span["name"] == "Running test_basic":
            assert span["kind"] == "SpanKind.INTERNAL"
            assert span["status"]["status_code"] == "OK"
            assert span["attributes"]["test.name"] == "test_basic"
            assert span["attributes"]["test.outcome"] == "passed"
            assert len(span["parent_id"]) > 0
            foundTestBasic = True
        if span["name"] == "Test Suite":
            assert span["kind"] == "SpanKind.SERVER"
            assert span["status"]["status_code"] == "OK"
            assert span["attributes"]["test.outcome"] == "passed"
            assert span["parent_id"] is None
            foundTestSuit = True
    assert foundTestBasic
    assert foundTestSuit
#
# def test_success():
#     """Success."""
#     sleep_random_number()
#     assert True
#
# def test_failure():
#     """Failure."""
#     sleep_random_number()
#     assert 1 < 0
#
# def test_failure_code():
#     """Failure Code."""
#     sleep_random_number()
#     d = 1/0
#     assert True
#
# @pytest.mark.skip
# def test_skip():
#     """Skip."""
#     sleep_random_number()
#     assert True
#
# @pytest.mark.xfail
# def test_xfail():
#     """XFail."""
#     sleep_random_number()
#     assert False
#
# @pytest.mark.xfail(run=False)
# def test_xfail_no_run():
#     """XFail No Run."""
#     sleep_random_number()
#     assert False

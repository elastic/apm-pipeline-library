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

import time
import random
import pytest

def sleep_random_number():
    time.sleep(random.randint(3, 10))

def test_basic():
    """Basic."""
    sleep_random_number()
    pass

def test_success():
    """Success."""
    sleep_random_number()
    assert True

def test_failure():
    """Failure."""
    sleep_random_number()
    assert 1 < 0

def test_failure_code():
    """Failure Code."""
    sleep_random_number()
    d = 1/0
    assert True

@pytest.mark.skip
def test_skip():
    """Skip."""
    sleep_random_number()
    assert True

@pytest.mark.xfail
def test_xfail():
    """XFail."""
    sleep_random_number()
    assert False

@pytest.mark.xfail(run=False)
def test_xfail_no_run():
    """XFail No Run."""
    sleep_random_number()
    assert False

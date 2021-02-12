#!/usr/bin/env python
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

from setuptools import setup

setup(
    name='apm-cli',
    version='0.0.1',
    python_requires=">=3.5",
    packages=["apm"],
    install_requires=[
        "elastic-apm",
        "ConfigArgParse",
        "psutil",
    ],
    url='https://github.com/elastic/apm-pipeline-library/tree/master/resources/scripts/apm-cli',
    license='Apache License Version 2.0',
    description='APM command line tool to create APM transactions and spans',
    scripts=['apm-cli.py'],
)

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
    name='pytest-otel',
    version='0.0.1',
    py_modules=["pytest_otel"],
    entry_points={"pytest11": ["otel = pytest_otel"]},
    install_requires=["setuptools>=40.0", "pytest >= 5.0", "opentelemetry-api==1.2.0", "opentelemetry-exporter-otlp==1.2.0", "opentelemetry-sdk==1.2.0"],
    python_requires=">=3.5",
    url='https://github.com/elastic/apm-pipeline-library/tree/master/resources/scripts/pytest_otel',
    license='Apache License Version 2.0',
    description='OpenTelemetry pytest plugin for report OpenTelemetry traces about test executed.',
    classifiers=["Framework :: Pytest"],
)

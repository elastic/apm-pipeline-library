#!/usr/bin/env python

from setuptools import setup

setup(
    name='pytest-apm',
    version='0.0.1',
    py_modules=["pytest_apm"],
    entry_points={"pytest11": ["apm = pytest_apm"]},
    install_requires=["setuptools>=40.0", "pytest >= 5.0"],
    python_requires=">=3.5",
    url='https://github.com/elastic/apm-pipeline-library/tree/master/resources/scripts/apm-cli',
    license='Apache License Version 2.0',
    description='APM pytest plugin for report APM traces about test executed.',
    classifiers=["Framework :: Pytest"],
)

from setuptools import setup

setup(
    name='apm-cli',
    version='0.0.1',
    install_requires=[
        "elastic-apm",
        "ConfigArgParse",
        "psutil"
    ],
    url='https://github.com/elastic/observability-robots/tree/master/apps/apm-cli',
    license='Apache License Version 2.0',
    description='APM command line tool to create APM transactions and spans',
)

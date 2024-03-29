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

import logging

import configargparse
import sys
from elasticapm import Client, set_context, capture_span, get_trace_id, trace_parent_from_string, get_span_id
import json
import subprocess


class ApmCliArgs:

    def __init__(self):
        self.apm_server_url = None
        self.apm_token = None
        self.api_key = None
        self.service_name = None
        self.custom_context = None
        self.transaction_name = None
        self.transaction_result = None
        self.span_name = None
        self.span_type = None
        self.span_subtype = None
        self.span_labels = None
        self.span_action = None
        self.span_cmd = None
        self.apm_parent_id_file_save = None
        self.apm_parent_id_file_load = None
        self.apm_parent_id = None


class ApmCli:
    def file_for_read(self, path):
        """
        Open a fine for reading.

        :param path: Path for the file.
        :return: An File type object for reading.
        """
        return open(path, "r")

    def file_for_write(self, path):
        """
        Open a file for writing.

        :param path: Path for the file.
        :return: An File type object for writing.
        """
        return open(path, "w")

    def prepare_arguments(self, cmd_args=None):
        """
        This method define the parameters we can use from command line and the environment variables associated with them.

        :return: the arguments parsed from command line.
        """
        parser = configargparse.ArgParser(description='Command line tool to generate APM traces. '
                                                      + 'see https://www.elastic.co/guide/en/apm/agent/python/current/api.html')  # noqa E501
        parser.add_argument('--apm-server-url',
                            dest='apm_server_url',
                            env_var='APM_CLI_SERVER_URL',
                            default='http://nowhere.example.com',
                            required=True,
                            help='URL for the APM server.')
        group_auth = parser.add_mutually_exclusive_group(required=True)
        group_auth.add_argument('--apm-token',
                            dest='apm_token',
                            env_var='APM_CLI_TOKEN',
                            help='Token to access to APM server.')
        group_auth.add_argument('--apm-api-key',
                            dest='api_key',
                            env_var='APM_CLI_TOKEN_API_KEY',
                            help='API key to access to APM server.')

        parser.add_argument('--service-name',
                            dest='service_name',
                            env_var='APM_CLI_SERVICE_NAME',
                            required=True,
                            help='Name of the service.')
        parser.add_argument('--custom-context',
                            dest='custom_context',
                            env_var='APM_CLI_CUSTOM_CONTEXT',
                            help='Custom context for the current transaction in a JSON string.')

        ts_group = parser.add_argument_group(title='Transaction')
        ts_group.add_argument('--transaction-name',
                            dest='transaction_name',
                            env_var='APM_CLI_TRANSACTION_NAME',
                            required=True,
                            help='Name of the transaction.')
        ts_group.add_argument('--transaction-result',
                            dest='transaction_result',
                            default='success',
                            env_var='APM_CLI_TRANSACTION_RESULT',
                            help='Result for the active transaction.')

        span_group = parser.add_argument_group(title='Span')
        span_group.add_argument('--span-name',
                            dest='span_name',
                            env_var='APM_CLI_SPAN_NAME',
                            help='Name of the span.')
        span_group.add_argument('--span-type',
                            dest='span_type',
                            default='command',
                            env_var='APM_CLI_SPAN_TYPE',
                            help='Type for the active span.')
        span_group.add_argument('--span-subtype',
                            dest='span_subtype',
                            env_var='APM_CLI_SPAN_SUBTYPE',
                            help='SubType for the active span.')
        span_group.add_argument('--span-labels',
                            dest='span_labels',
                            env_var='APM_CLI_SPAN_LABELS',
                            default='{}',
                            help='Labels for the active span.')
        span_group.add_argument('--span-action',
                            dest='span_action',
                            env_var='APM_CLI_SPAN_ACTION',
                            help='Action for the active span.')
        span_group.add_argument('--span-command',
                            dest='span_cmd',
                            env_var='APM_CLI_SPAN_COMMAND',
                            help='Command to execute in a shell for the active span.')

        parent_group = parser.add_argument_group(title='Parent Transaction')
        parent_group.add_argument('--parent-transaction-save',
                            dest='apm_parent_id_file_save',
                            env_var='APM_CLI_PARENT_TRANSACTION_SAVE',
                            type=self.file_for_write,
                            help='File to save the parent transaction ID.')
        parent_group.add_argument('--parent-transaction-load',
                            dest='apm_parent_id_file_load',
                            env_var='APM_CLI_PARENT_TRANSACTION_LOAD',
                            type=self.file_for_read,
                            help='File to load the parent transaction ID.')
        parent_group.add_argument('--parent-transaction',
                            dest='apm_parent_id',
                            env_var='APM_CLI_PARENT_TRANSACTION',
                            help='The parent transaction ID.')
        return parser.parse_args(cmd_args)

    def begin_span(self):
        """
        Captures a new span. You can provide a command to execute.

        :param apm_client: APM client.
        :param args: options loaded from command line or environment variables.
        :return: None
        """
        if self.args.span_name:
            with capture_span(self.args.span_name,
                              span_type=self.args.span_type,
                              span_subtype=self.args.span_subtype,
                              span_action=self.args.span_action,
                              labels=json.loads(self.args.span_labels if self.args.span_labels else '{}')) as span:
                self.logger.debug('The span begins.')
                if self.args.span_cmd:
                    output = ''
                    try:
                        self.logger.debug('Executing the command.')
                        output = subprocess.check_output(self.args.span_cmd, shell=True).decode('utf8').strip()
                        self.logger.debug(output)
                        self.logger.debug('The span ends.')
                        self.span = span
                    except subprocess.CalledProcessError:
                        self.apm_client.capture_exception()
                        self.logger.debug('Error executing the command.')
                        self.logger.debug(output)
                        self.logger.debug('The span ends.')
                        self.span = span
                        sys.exit(1)
                else:
                    self.logger.debug('The span ends.')

    def get_parent_id(self):
        """
        :return: returns the parent ID of the current transaction.
        """
        return "{:02x}-{}-{}-{:02x}".format(0, get_trace_id(), get_span_id(), 1)

    def begin_transaction(self):
        """
        Begins a new transaction. You can provide a parent transaction to enable distributed tracing.

        :param apm_client: APM client.
        :param args: options loaded from command line or environment variables.
        :return: None
        """
        if self.args.apm_parent_id_file_load:
            ts_id = self.args.apm_parent_id_file_load.read()
            self.args.apm_parent_id_file_load.close()
            self.parent = trace_parent_from_string(ts_id)
            self.transaction = self.apm_client.begin_transaction(self.args.transaction_name, trace_parent=self.parent)
            self.logger.debug('Parent transaction : ' + ts_id)
        elif self.args.apm_parent_id:
            self.parent = trace_parent_from_string(self.args.apm_parent_id)
            self.transaction = self.apm_client.begin_transaction(self.args.transaction_name, trace_parent=self.parent)
            self.logger.debug('Parent transaction : ' + self.args.apm_parent_id)
        else:
            self.transaction = self.apm_client.begin_transaction(self.args.transaction_name)
        self.logger.debug('The transaction begins.')
        if self.args.apm_parent_id_file_save:
            with capture_span('parent'):
                self.args.apm_parent_id_file_save.write(self.get_parent_id())
                self.args.apm_parent_id_file_save.close()
        return self.transaction

    def init_apm_client(self):
        """
        Initializes the APM client.

        :param args: options loaded from command line or environment variables.
        :return: the APM client object.
        """
        apm_client = None
        if self.args.apm_token:
            apm_client = Client(service_name=self.args.service_name,
                                server_url=self.args.apm_server_url,
                                verify_server_cert=False,
                                secret_token=self.args.apm_token,
                                use_elastic_traceparent_header=True,
                                debug=True)
        elif self.args.api_key:
            apm_client = Client(service_name=self.args.service_name,
                                server_url=self.args.apm_server_url,
                                verify_server_cert=False,
                                api_key=self.args.api_key,
                                use_elastic_traceparent_header=True,
                                debug=True)
        return apm_client

    def set_context(self, key="custom"):
        """
        Attach contextual data to the current transaction and errors that happen during the current transaction.

        :param key: the namespace for this data.
        """
        if self.args.custom_context:
            set_context(json.loads(self.args.custom_context), key)

    def end_transaction(self):
        self.apm_client.end_transaction(self.args.transaction_name, self.args.transaction_result)
        self.logger.debug('The transaction ends.')

    def __init__(self, cmd_args=None, parse_cmd_args=True):
        """

        :param cmd_args: Optional parameter to pass an array of command line arguments for parsing instead of use sys.args.
        :param parse_cmd_args: False to do not parse command line arguments and init the APM Client see the init method.
        """  # noqa E501
        self.logger = logging.getLogger("ApmCli")
        self.transaction = None
        self.span = None
        self.parent = None
        if parse_cmd_args:
            self.args = self.prepare_arguments(cmd_args)
            self.apm_client = self.init_apm_client()

    def init(self, server_url=None, apm_token=None, api_key=None, service_name=None):
        """

        :param server_url: URL of the APM Server.
        :param apm_token: Token to access to the APM Server.
        :param api_key: API key to access to the APM Server (conflits with Token).
        :param service_name: NAme of the service to report in the APM traces.
        """
        self.logger = logging.getLogger("ApmCli")
        if not server_url and (apm_token or api_key) and service_name:
            self.logger.error("APM server URL, APM service name, and an TOKEN or API Key are required to connect to the APM service.")  # noqa E501
            raise
        self.transaction = None
        self.span = None
        self.parent = None
        self.args = ApmCliArgs()
        self.args.apm_server_url = server_url
        self.args.apm_token = apm_token
        self.args.api_key = api_key
        self.args.service_name = service_name
        self.args.apm_token = apm_token
        self.apm_client = self.init_apm_client()

    def run(self):
        """
        Send APM data based on the command line options.

        :return: None
        """
        self.begin_transaction()
        self.set_context()
        self.begin_span()
        self.end_transaction()

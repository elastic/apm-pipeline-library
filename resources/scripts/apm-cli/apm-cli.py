#!/usr/bin/env python3
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

import configargparse
import sys
from elasticapm import Client, set_custom_context, capture_span, get_trace_id, trace_parent_from_string, get_span_id
import json
import subprocess


def file_for_read(path):
    """
    Open a fine for reading.

    :param path: Path for the file.
    :return: An File type object for reading.
    """
    return open(path, "r")


def file_for_write(path):
    """
    Open a file for writting.

    :param path: Path for the file.
    :return: An File type object for writting.
    """
    return open(path, "w")


def prepare_arguments():
    """
    This method define the parameters we can use from command line and the environment variables associated with them.

    :return: the arguments parsed from command line.
    """
    parser = configargparse.ArgParser(description='Command line tool to generate APM traces. '
                                                  + 'see https://www.elastic.co/guide/en/apm/agent/python/current/api.html')  # noqa E501
    parser.add_argument('-sn', '--service-name',
                        dest='service_name',
                        env_var='APM_CLI_SERVICE_NAME',
                        required=True,
                        help='Name of the service.')
    parser.add_argument('-t', '--transaction-name',
                        dest='transaction_name',
                        env_var='APM_CLI_TRANSACTION_NAME',
                        required=True,
                        help='Name of the transaction.')
    parser.add_argument('-tr', '--transaction-result',
                        dest='transaction_result',
                        default='success',
                        env_var='APM_CLI_TRANSACTION_RESULT',
                        help='Result for the active transaction.')
    parser.add_argument('-s', '--span-name',
                        dest='span_name',
                        env_var='APM_CLI_SPAN_NAME',
                        help='Name of the span.')
    parser.add_argument('-st', '--span-type',
                        dest='span_type',
                        default='command',
                        env_var='APM_CLI_SPAN_TYPE',
                        help='Type for the active span.')
    parser.add_argument('-sst', '--span-subtype',
                        dest='span_subtype',
                        env_var='APM_CLI_SPAN_SUBTYPE',
                        help='SubType for the active span.')
    parser.add_argument('-sl', '--span-labels',
                        dest='span_labels',
                        env_var='APM_CLI_SPAN_LABELS',
                        default='{}',
                        help='Labels for the active span.')
    parser.add_argument('-sa', '--span-action',
                        dest='span_action',
                        env_var='APM_CLI_SPAN_ACTION',
                        help='Action for the active span.')
    parser.add_argument('-sc', '--span-command',
                        dest='span_cmd',
                        env_var='APM_CLI_SPAN_COMMAND',
                        help='Command to execute in a shell for the active span.')
    parser.add_argument('-c', '--custom-context',
                        dest='custom_context',
                        env_var='APM_CLI_CUSTOM_CONTEXT',
                        help='Custom context for the current transaction in a JSON string.')
    parser.add_argument('--apm-server-url',
                        dest='apm_server_url',
                        env_var='APM_CLI_SERVER_URL',
                        default='http://nowhere.example.com',
                        help='URL for the APM server.')
    parser.add_argument('--apm-token',
                        dest='apm_token',
                        env_var='APM_CLI_TOKEN',
                        default='NO_SET',
                        help='URL for the APM server.')
    parser.add_argument('--parent-transaction-save',
                        dest='apm_parent_id_file_save',
                        env_var='APM_CLI_PARENT_TRANSACTION_SAVE',
                        type=file_for_write,
                        help='File to save the parent transaction ID.')
    parser.add_argument('--parent-transaction-load',
                        dest='apm_parent_id_file_load',
                        env_var='APM_CLI_PARENT_TRANSACTION_LOAD',
                        type=file_for_read,
                        help='File to load the parent transaction ID.')
    parser.add_argument('--parent-transaction',
                        dest='apm_parent_id',
                        env_var='APM_CLI_PARENT_TRANSACTION',
                        help='The parent transaction ID.')
    return parser.parse_args()


def main():
    """
    Send APM data based on the command line options.

    :return: None
    """
    args = prepare_arguments()
    apm_client = init_apm_client(args)
    transaction(apm_client, args)
    if args.custom_context:
        set_custom_context(json.loads(args.custom_context))
    span(apm_client, args)
    apm_client.end_transaction(args.transaction_name, args.transaction_result)
    print('The transaction ends.')


def span(apm_client, args):
    """
    Captures a new span. You can provide a command to execute.

    :param apm_client: APM client.
    :param args: options loaded from command line or environment variables.
    :return: None
    """
    if args.span_name:
        with capture_span(args.span_name,
                          span_type=args.span_type,
                          span_subtype=args.span_subtype,
                          span_action=args.span_action,
                          labels=json.loads(args.span_labels)):
            print('The span begins.')
            if args.span_cmd:
                output = ''
                try:
                    print('Executing the command.')
                    output = subprocess.check_output(args.span_cmd, shell=True).decode('utf8').strip()
                    print(output)
                    print('The span ends.')
                except subprocess.CalledProcessError:
                    apm_client.capture_exception()
                    print('Error executing the command.')
                    print(output)
                    print('The span ends.')
                    sys.exit(1)
            else:
                print('The span ends.')
                sys.exit(0)


def transaction(apm_client, args):
    """
    Begins a new transaction. You can provide a parent transaction to enable distributed tracing.

    :param apm_client: APM client.
    :param args: options loaded from command line or environment variables.
    :return: None
    """
    if args.apm_parent_id_file_load:
        ts_id = args.apm_parent_id_file_load.read()
        args.apm_parent_id_file_load.close()
        parent = trace_parent_from_string(ts_id)
        apm_client.begin_transaction(args.transaction_name, trace_parent=parent)
        print('Parent transaction : ' + parent.to_string())
    elif args.apm_parent_id:
        parent = trace_parent_from_string(args.apm_parent_id)
        apm_client.begin_transaction(args.transaction_name, trace_parent=parent)
        print('Parent transaction : ' + parent.to_string())
    else:
        apm_client.begin_transaction(args.transaction_name)
    print('The transaction begins.')
    if args.apm_parent_id_file_save:
        with capture_span('parent'):
            args.apm_parent_id_file_save.write("{:02x}-{}-{}-{:02x}".format(0, get_trace_id(), get_span_id(), 1))
            args.apm_parent_id_file_save.close()


def init_apm_client(args):
    """
    Initializes tha APM client.

    :param args: options loaded from command line or environment variables.
    :return: the APM client object.
    """
    apm_client = None
    if args.apm_token:
        apm_client = Client(service_name=args.service_name,
                            server_url=args.apm_server_url,
                            verify_server_cert=False,
                            secret_token=args.apm_token,
                            use_elastic_traceparent_header=True,
                            debug=True)
    elif args.api_key:
        apm_client = Client(service_name=args.service_name,
                            server_url=args.apm_server_url,
                            verify_server_cert=False,
                            api_key=args.api_key,
                            use_elastic_traceparent_header=True,
                            debug=True)
    return apm_client


if __name__ == '__main__':
    main()
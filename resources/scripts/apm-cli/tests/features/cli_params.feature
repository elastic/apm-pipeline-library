Feature: CLI
    Running the tool from command line.

    Scenario: Basic transaction
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        When I launch the apm-cly.py
        Then a transaction is reported

    Scenario: Basic transaction with api key
        Given an APM server URL
        And a api key
        And a service name
        And a transaction name
        When I launch the apm-cly.py
        Then a transaction is reported

    Scenario: Basic transaction with custom context
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        And a custom context
        When I launch the apm-cly.py
        Then a transaction is reported
        And the context is set

    Scenario: Basic transaction with a transaction result
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        And a transaction result custom_result
        When I launch the apm-cly.py
        Then a transaction is reported
        And the transaction result is custom_result

    Scenario: Basic transaction and span
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        And a span name
        And a span type
        And a span subtype
        And a span label
        And a span action
        And a span command
        When I launch the apm-cly.py
        Then a transaction is reported
        And a span is reported

    Scenario: Save parent transaction ID
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        And a file to save the parent transaction ID
        When I launch the apm-cly.py
        Then a transaction is reported
        And the file with the parent transaction ID exits

    Scenario: Load parent transaction ID
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        And a file to load the parent transaction ID
        When I launch the apm-cly.py
        Then a transaction is reported
        And a parent ID is set

    Scenario: Set parent transaction ID
        Given an APM server URL
        And a token
        And a service name
        And a transaction name
        And a parent transaction ID
        When I launch the apm-cly.py
        Then a transaction is reported
        And a parent ID is set

    Scenario: Missing APM config Command line parameter
        Given a transaction name
        And a service name
        When I launch the apm-cly.py
        Then it fails to start

    Scenario: Missing service name Command line parameter
        Given an APM server URL and a token
        And a transaction name
        When I launch the apm-cly.py
        Then it fails to start

    Scenario: Missing transaction Command line parameter
        Given an APM server URL and a token
        And a service name
        When I launch the apm-cly.py
        Then it fails to start

    Scenario: Api Key and Token Command line parameters passed
        Given an APM server URL
        And a token
        And a api key
        And a service name
        And a transaction name
        When I launch the apm-cly.py
        Then it fails to start

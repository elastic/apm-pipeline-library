Feature: IsTimerTrigger

Scenario Outline: The build step is only triggered by Cron events
Given the "IsTimerTrigger" job is present
When the build is triggered by a "<event_type>" event
Then the result of the build step is "<result>"
Examples:
| event_type  | result |
| cron        | true   |
| manually    | false  |
| git-push    | false  |
| git-polling | false  |

Feature: Retrieve todos from a project

    As a user,
    I want to retrieve all todos associated with a project,
    so that I can view what tasks need to be completed.

    Background: The server is running
        Given the server is running for Story10
        And a project with id 1 exists
        And the project has the following tasks
            | title          | completed |
            | "Write report" | false     |
            | "Review notes" | true      |

    Scenario: Retrieve todos from an existing project
        When the user requests tasks for project with id 1
        Then the server should respond with status 200
        And the user should receive the following tasks
            | title          | completed |
            | "Write report" | false     |
            | "Review notes" | true      |

    Scenario: Retrieve todos from a non-existent project (Error Flow)
        Given a project with id 999 does not exist
        When the user requests tasks for project with id 999
        Then the server should respond with an error status 404
        And the user should not receive any tasks

    Scenario: Retrieve todos for an existing project with no tasks (Alternate Flow)
        Given a project with id 2 exists
        When the user requests tasks for project with id 2
        Then the server should respond with status 200
        And the user should not receive any tasks

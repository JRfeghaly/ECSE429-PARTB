Feature: Permanently Delete a Study Project

    As a user, I want to permanently delete a project that I no longer need,
    so that my project list remains clean and organized.

    Background: The server is running
        Given the server is running for permanent delete
        And the following projects exist for permanent delete
            | id | title         | completed | description        | active |
            | 1  | "ECSE 429"    | "true"    | "Midterm completed"| "false" |
            | 2  | "COMP 251"    | "false"   | "Study graphs"     | "true"  |

    Scenario Outline: Permanently delete an existing project
        Given a user has a project with permanent delete ID <id>
        When the user requests to permanently delete the project with permanent delete ID <id>
        Then the project with permanent delete ID <id> is removed from the project list
        And the user is notified of the successful deletion

        Examples:
            | id |
            | 1  |

    Scenario Outline: Delete a non-existent project (Error Flow)
        Given a project with permanent delete ID <id> does not exist
        When the user requests to permanently delete the project with permanent delete ID <id>
        Then the user is notified with an error message <message>

        Examples:
            | id  | message                                         |
            | 99  | "Could not find any instances with projects/99" |

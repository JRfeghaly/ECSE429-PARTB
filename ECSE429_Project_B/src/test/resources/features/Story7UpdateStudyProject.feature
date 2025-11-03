Feature: Update a Study Project

    As a student, I want to update the details of an existing study project,
    so I can keep the information current as my schedule changes.

    Background: The server is up and running
        Given the server is running
        And the following study projects exist
            | id | title        | completed | description           | active |
            | 1  | "ECSE 429"   | "false"   | "Calculus 2"          | "true" |
            | 2  | "ENGL 202"   | "false"   | "Literature Review"   | "true" |

    Scenario Outline: Update details of an existing study project (Normal Flow)
        Given a student has a study project with id <id>
        When the student updates the project to have <title>, <completed>, <description>, and <active>
        Then the project with id <id> is updated with the new details
        And the student is notified of the completion of the update operation

        Examples:
            | id | title        | completed | description                    | active |
            | 1  | "ECSE 429"   | "true"    | "Finished all midterm reviews" | "true" |
            | 2  | "ENGL 202"   | "false"   | "Essay draft in progress"      | "true" |

    Scenario Outline: Update only one field of a study project (Alternate Flow)
        Given a student has a study project with id <id>
        When the student updates only the <description> of the study project
        Then the project with id <id> reflects the updated <description>
        And the student is notified of the completion of the update operation

        Examples:
            | id | description                  |
            | 1  | "Need to review integration" |
            | 2  | "Reading Act II tonight"     |

    Scenario Outline: Update a non-existent study project (Error Flow)
        Given a study project with ID of <id> does not exist
        When the student requests to update the project with id <id> to change its details
        Then the student is notified of the non-existence error with a message <message>

        Examples:
            | id  | message                                         |
            | 99  | "Could not find any instances with projects/99" |

Feature: Create a Study Project

    As a user, I want to create a new project so I can start organizing my tasks.

    Background: Server is running, Project Todo List is created
        Given the server is running

    Scenario Outline: Create a study project (Normal Flow)
        When a user creates a new project with <title>, <completed>, <description>, and <active>
        Then the project is created successfully with <title>, <completed>, <description>, and <active>
        And the user is notified of the successful creation

        Examples:
            | title           | completed | description            | active |
            | "ECSE 223"      | "false"   | "Software Engineering" | "true" |
            | "COMP 251"      | "false"   | "Algorithms Review"    | "true" |

    Scenario Outline: Create a study project without a title (Alternate Flow)
        When a user creates a new project with <completed>, <description>, and <active>
        Then the project list is created with <completed>, <description>, and <active>
        And the user is notified that the title field is required

        Examples:
            | completed | description            | active |
            | "false"   | "Software Engineering" | "true" |
            | "false"   | "Algorithms Review"    | "true" |

    Scenario Outline: Create a study project with invalid input type for active status (Error Flow)
        When a user creates a new project with <title>, <completed>, <description>, and wrong <bad_active>
        Then the user is notified of the failed validation with a message <message>

        Examples:
            | title           | completed | description            | bad_active | message                                        |
            | "ECSE 223"      | "false"   | "Software Engineering" | "yes"      | "Failed Validation: active should be BOOLEAN"  |
            | "COMP 251"      | "false"   | "Algorithms Review"    | "active"   | "Failed Validation: active should be BOOLEAN"  |

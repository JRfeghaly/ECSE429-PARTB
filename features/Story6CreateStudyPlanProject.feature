Feature: Create a Study Project

    As a student, I want to create a new study project so I can start organizing and tracking my study tasks.

    Background: Server is running, Study Plan Todo List is created
        Given the server is running

    Scenario Outline: Create a study project (Normal Flow)
        When a student creates a new study project with <title>, <completed>, <description>, and <active>
        Then the study project is created successfully with <title>, <completed>, <description>, and <active>
        And the student is notified of the successful creation

        Examples:
            | title           | completed | description            | active |
            | "ECSE 223"      | "false"   | "Software Engineering" | "true" |
            | "COMP 251"      | "false"   | "Algorithms Review"    | "true" |

    Scenario Outline: Create a study project without a title (Alternate Flow)
        When a student creates a new study project with <completed>, <description>, and <active>
        Then the study project list is created with <completed>, <description>, and <active>
        And the student is notified that the title field is required

        Examples:
            | completed | description            | active |
            | "false"   | "Software Engineering" | "true" |
            | "false"   | "Algorithms Review"    | "true" |

    Scenario Outline: Create a study project with invalid input type for active status (Error Flow)
        When a student creates a new study project with <title>, <completed>, <description>, and wrong <bad_active>
        Then the student is notified of the failed validation with a message <message>

        Examples:
            | title           | completed | description            | bad_active | message                                        |
            | "ECSE 223"      | "false"   | "Software Engineering" | "yes"      | "Failed Validation: active should be BOOLEAN"  |
            | "COMP 251"      | "false"   | "Algorithms Review"    | "active"   | "Failed Validation: active should be BOOLEAN"  |

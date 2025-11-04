Feature: Create a New Category

  As a user, I want to create new categories so that I can organize my TODOs and projects effectively.

  Background: Server is running and no categories exist
    Given the server is running for category creation
    And the system has no existing categories

  Scenario Outline: Successfully creating a category with valid title and description (Normal Flow)
    When the user creates a category with title <title> and description <description>
    Then the category with title <title> should be successfully created
    And the user is notified of the successful creation operation

    Examples:
      | title         | description            |
      | "School Work" | "Assignments and labs" |
      | "Personal"    | "Personal reminders"   |
      | "Fitness"     | "Workout tracking"     |

  Scenario Outline: Creating a category without an optional description (Alternate Flow)
    When the user creates a category with title <title> and no description
    Then the category with title <title> should be successfully created
    And the user is notified of the successful creation operation

    Examples:
      | title        |
      | "Work"       |
      | "Chores"     |
      | "Reading"    |

  Scenario Outline: Attempting to create a category with an empty title (Error Flow)
    When the user creates a category with title <title> and description <description>
    Then the API should respond with an error message <message>

    Examples:
      | title | description         | message                    |
      | ""    | "Invalid category"  | "Invalid input parameters" |

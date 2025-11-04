Feature: Add a New Todo

  As a user, I want to add a new todo so that I can track new tasks to complete.

  Background: Server is running
    Given the server is running

  Scenario Outline: Adding a valid new todo (Normal Flow)
    When the user creates a new todo with title <title> and description <description>
    Then the response status should be 201
    And the response contains a generated id
    And the todo with title <title> exists

    Examples:
      | title            | description                |
      | "Plan trip"      | "Book flights and hotel"   |
      | "Clean kitchen"  | "Organize pantry"          |

  Scenario: Adding a todo without a title (Error Flow)
    When the user creates a todo with empty title and description "No title"
    Then the response status should be 400
    And the response contains error message with keyword "title"

  Scenario Outline: Adding a todo with duplicate title (Alternate Flow)
    When the user creates a new todo with title <title> and description <description>
    Then the response status should be 201
    And the response contains a generated id

    Examples:
      | title           | description |
      | "Buy groceries" | "Food"      |

Feature: View All Todos

  As a user, I want to view all existing todos so that I can see my pending and completed tasks.

  Background: Server is running and todos exist
    Given the server is running
    And at least one todo exists

  Scenario: Retrieving all existing todos (Normal Flow)
    When the user retrieves all todos
    Then the response status should be 200
    And the response contains a list of todos

  Scenario Outline: Retrieving a specific todo by ID (Alternate Flow)
    When the user retrieves the todo with id <id>
    Then the response status should be 200
    And the response contains the todo with id <id>

    Examples:
      | id |
      | 1  |
      | 3  |

  Scenario Outline: Retrieving a non-existent todo by ID (Error Flow)
    When the user retrieves the todo with id <id>
    Then the response status should be 404

    Examples:
      | id  |
      | 99  |
      | 500 |

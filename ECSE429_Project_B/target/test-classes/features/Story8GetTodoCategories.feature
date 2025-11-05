Feature: Retrieve categories from a todo

  As a user,
  I want to view the categories linked to a specific todo,
  So that I can understand how each todo is organized.

  Background:
    Given the server is running for Story8

  Scenario: Retrieve categories for an existing todo
    Given a todo exists with id 1
    And the todo with id 1 has the following categories
      | name      |
      | Work      |
      | Personal  |
    When the user requests categories for todo with id 1
    Then the server should respond with status code 200 for Story8
    And the user should receive the following categories
      | name      |
      | Work      |
      | Personal  |

  Scenario: Retrieve categories for a non-existent todo (Error Flow)
    Given a todo with id 99 does not exist
    When the user requests categories for todo with id 99
    Then the server should respond with status code 404 for Story8
    And the user should receive a warning that categories are invalid

  Scenario: Retrieve categories for an existing todo with no categories (Alternate Flow)
    Given a todo exists with id 2
    When the user requests categories for todo with id 2
    Then the server should respond with status code 200 for Story8
    And the user should receive the following categories
      | name |

Feature: Search Categories

  As a user, I want to search categories so that I can quickly view or locate specific groups of my TODOs and projects.

  Background: Server is running and several categories exist
    Given the server is running for search category
    And categories with the following details exist for search category
      | title          | description              |
      | "School Work"  | "Assignments and labs"   |
      | "Personal"     | "Daily personal tasks"   |
      | "Fitness"      | "Workout and nutrition"  |
      | "Hobbies"      | "Creative weekend plans" |

  Scenario: Retrieving all existing categories (Normal Flow)
    When the user retrieves all categories
    Then the server responds with status 200
    And the response contains all existing categories
    And each category includes fields "id", "title", and "description"

  Scenario Outline: Retrieving a category by its stored ID (Alternate Flow)
    When the user retrieves the category with stored id for <title>
    Then the server responds with status 200
    And the response includes a category with title <title> and description <description>

    Examples:
      | title         | description              |
      | "School Work" | "Assignments and labs"   |
      | "Fitness"     | "Workout and nutrition"  |

  Scenario: Retrieving a non-existent category (Error Flow)
    When the user retrieves a category that does not exist
    Then the server responds with status 404
    And the user is notified that no results were found

Feature: Search Categories

  As a user, I want to search categories so that I can quickly view or locate specific groups of my TODOs and projects.

  Background: Server is running and several categories exist
    Given the server is running
    And categories with the following details exist
      | id | title          | description              |
      | 1  | "School Work"  | "Assignments and labs"   |
      | 2  | "Personal"     | "Daily personal tasks"   |
      | 3  | "Fitness"      | "Workout and nutrition"  |
      | 4  | "Hobbies"      | "Creative weekend plans" |

  Scenario: Retrieving all existing categories (Normal Flow)
    When the user retrieves all categories
    Then the server responds with status 200
    And the response contains all existing categories
    And each category includes fields "id", "title", and "description"

  Scenario Outline: Retrieving a specific category by ID (Normal Flow)
    When the user retrieves the category with id <id>
    Then the server responds with status 200
    And the response includes a category with title <title> and description <description>

    Examples:
      | id | title         | description              |
      | 1  | "School Work" | "Assignments and labs"   |
      | 3  | "Fitness"     | "Workout and nutrition"  |

  Scenario Outline: Searching categories by title filter (Alternate Flow)
    When the user retrieves categories filtered by title <title>
    Then the server responds with status 200
    And the response contains only categories whose title matches <title>

    Examples:
      | title          |
      | "Personal"     |
      | "Hobbies"      |

  Scenario Outline: Searching categories by description filter (Alternate Flow)
    When the user retrieves categories filtered by description <description>
    Then the server responds with status 200
    And the response contains only categories whose description includes <description>

    Examples:
      | description            |
      | "Workout"              |
      | "Assignments"          |

  Scenario Outline: Retrieving a non-existent category by ID (Error Flow)
    When the user retrieves the category with id <non_existing_id>
    Then the server responds with status 404
    And the user is notified with message <message>

    Examples:
      | non_existing_id | message                     |
      | 99              | "Could not find category 99" |
      | 500             | "Could not find category 500" |

  Scenario Outline: Searching categories with a title that does not exist (Error Flow)
    When the user retrieves categories filtered by title <title>
    Then the server responds with status 200
    And the response contains no matching categories
    And the user is notified that no results were found

    Examples:
      | title         |
      | "Nonexistent" |
      | "Unknown"     |

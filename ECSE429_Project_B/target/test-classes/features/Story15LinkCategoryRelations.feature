Feature: Link TODOs and Projects to Categories

  As a user, I want to link TODOs and projects to categories so that I can organize related tasks and projects together.

  Background: Server is running, categories, TODOs, and projects exist
    Given the server is running
    And a category with id 3 and title "University" exists
    And TODOs with the following details exist
      | id | title               | doneStatus | description        |
      | 10 | "Complete Assignment" | false      | "CS homework"      |
    And Projects with the following details exist
      | id | title           | description        |
      | 20 | "Capstone Demo" | "Prepare slides"   |

  Scenario Outline: Linking a TODO to an existing category (Normal Flow)
    When the user links TODO with id <todo_id> to category with id <category_id>
    Then the server responds with status 201
    And the category with id <category_id> now includes TODO <todo_id>

    Examples:
      | todo_id | category_id |
      | 10      | 3           |

  Scenario Outline: Linking a TODO that is already linked (Alternate Flow)
    Given TODO with id <todo_id> is already linked to category <category_id>
    When the user links TODO with id <todo_id> to category with id <category_id>
    Then the server responds with status 200
    And no duplicate link is created

    Examples:
      | todo_id | category_id |
      | 10      | 3           |

  Scenario Outline: Linking a TODO to a non-existent category (Error Flow)
    When the user links TODO with id <todo_id> to category with id <invalid_category_id>
    Then the server responds with status 404
    And the user is notified with message <message>

    Examples:
      | todo_id | invalid_category_id | message                                    |
      | 10      | 999                 | "Could not find parent thing for relationship categories/999/todos" |

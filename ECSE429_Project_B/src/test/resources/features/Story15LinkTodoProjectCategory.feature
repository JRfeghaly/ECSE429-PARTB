Feature: Link TODOs and Projects to Categories

  As a user, I want to link TODOs and projects to categories so that I can organize related tasks and projects together.

  Background: Server is running, and the required entities exist
    Given the server is running for link category
    And a category with id 3 and title "University" exists
    And TODOs with the following details exist
      | id  | title                | doneStatus | description   |
      | 10  | "Complete Assignment" | false      | "CS homework" |
    And Projects with the following details exist
      | id  | title           | description      |
      | 20  | "Capstone Demo" | "Prepare slides" |

  Scenario: Linking a TODO to an existing category (Normal Flow)
    When the user links TODO with id 10 to category with id 3
    Then the link server responds with status 201
    And the category with id 3 now includes TODO 10

  Scenario: Linking a TODO that is already linked (Alternate Flow)
    Given TODO with id 10 is already linked to category 3
    When the user links TODO with id 10 to category with id 3 again
    Then the link server responds with status 201
    And no duplicate link is created

  Scenario: Linking a TODO to a non-existent category (Error Flow)
    When the user links TODO with id 10 to category with id 999 that does not exist
    Then the link server responds with status 404
    And the user is notified with link message "Could not find parent thing for relationship categories/999/todos"

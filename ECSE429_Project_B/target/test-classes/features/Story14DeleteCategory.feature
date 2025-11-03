Feature: Delete a Category

  Background:
    Given the server is running for delete category
    And categories with the following details exist for delete category
      | id | title       | description        |
      | 2  | "Temporary" | "Short-term tasks" |

  Scenario Outline: Deleting an existing category (Normal Flow)
    When the user deletes category with id <id>
    Then the delete server responds with status 200
    And the category with id <id> should no longer exist
    Examples:
      | id |
      | 2  |

  Scenario Outline: Deleting a category that was already deleted (Alternate Flow)
    Given category with id <id> was deleted previously
    When the user deletes category with id <id> again
    Then the delete server responds with status 404
    And the user is notified with delete message <message>
    Examples:
      | id | message                         |
      | 2  | "Category not found for deletion" |

  Scenario Outline: Deleting a category with invalid ID format (Error Flow)
    When the user deletes category with id <invalid_id>
    Then the delete server responds with status 400
    And the user is notified with message <message> for invalid delete
    Examples:
      | invalid_id | message             |
      | "abc"      | "Invalid ID format" |

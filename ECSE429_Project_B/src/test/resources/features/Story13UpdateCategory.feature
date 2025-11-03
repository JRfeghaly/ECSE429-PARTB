Feature: Update a Category

  As a user, I want to update an existing category so that I can keep my information accurate and organized.

  Background: Server is running and categories exist
    Given the server is running
    And categories with the following details exist
      | id | title      | description           |
      | 1  | "Personal" | "Reminders and tasks" |

  Scenario Outline: Updating a category’s title and description (Normal Flow)
    When the user updates category with id <id> to title <new_title> and description <new_description>
    Then the server responds with status 200
    And the category’s title is now <new_title>

    Examples:
      | id | new_title     | new_description     |
      | 1  | "Personal V2" | "Updated reminders" |

  Scenario Outline: Updating only the category’s description (Alternate Flow)
    When the user updates category with id <id> to description <new_description>
    Then the server responds with status 200
    And the category’s title remains unchanged

    Examples:
      | id | new_description   |
      | 1  | "Only description" |

  Scenario Outline: Updating a non-existent category (Error Flow)
    When the user updates category with id <invalid_id> to title <new_title>
    Then the server responds with status 404
    And the user is notified with message <message>

    Examples:
      | invalid_id | new_title       | message                |
      | 999        | "Ghost Category" | "Could not find category 999" |

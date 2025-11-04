Feature: Update an Existing Todo

  As a user, I want to update a todo so that I can modify its title or description.

  Background: Server is running and a todo exists
    Given the server is running
    And at least one todo exists

  Scenario: Updating a todo's title and description (Normal Flow)
    When the user updates todo with id 1 to have title "Buy groceries and snacks" and description "Buy fruits and chips"
    Then the response status should be 200
    And the todo with id 1 has title "Buy groceries and snacks"

  Scenario: Updating only the description (Alternate Flow)
    When the user updates todo with id 1 to have title "Buy groceries" and description "Only fruits"
    Then the response status should be 200
    And the todo with id 1 has title "Buy groceries"

  Scenario: Updating a non-existent todo (Error Flow)
    When the user updates todo with id 99 to have title "Ghost Todo" and description "Does not exist"
    Then the response status should be 404

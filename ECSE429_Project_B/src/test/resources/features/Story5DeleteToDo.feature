Feature: Delete a Todo

  As a user, I want to delete a todo so that I can remove tasks I no longer need.

  Background: Server is running and todos exist
    Given the server is running
    And at least one todo exists

  Scenario: Deleting an existing todo (Normal Flow)
    When the user deletes todo with id 1
    Then the response status should be 200

  Scenario: Deleting the same todo twice (Alternate Flow)
    When the user deletes todo with id 2
    And the user deletes todo with id 2
    Then the response status should be 404

  Scenario: Deleting a non-existent todo (Error Flow)
    When the user deletes todo with id 500
    Then the response status should be 404

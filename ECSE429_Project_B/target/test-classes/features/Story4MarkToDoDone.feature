Feature: Mark Todo as Done

  As a user, I want to mark a todo as done so that I can track completed tasks.

  Background: Server is running and todos exist
    Given the server is running
    And at least one todo exists

  Scenario: Marking a todo as done (Normal Flow)
    When the user marks todo with id 1 as done
    Then the response status should be 200

  Scenario: Marking an already done todo (Alternate Flow)
    When the user marks todo with id 1 as done
    Then the response status should be 200

  Scenario: Marking a non-existent todo as done (Error Flow)
    When the user marks todo with id 99 as done
    Then the response status should be 404

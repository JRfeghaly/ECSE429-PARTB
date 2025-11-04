@stories16to20
Feature: Story 20 – Retrieve all TODOs for a given Category

    As a user,
    I want to view all TODOs assigned to a specific category
    So that I can focus on tasks of the same priority or theme.

    Background: Server is running, base categories and TODOs exist
        Given the server is running
        And categories with the following details exist in 20
            | title           | description           |
            | High Priority   | urgent within 48 hrs  |
            | Backlog         | planned items         |
            | Reading         | study-related tasks   |
        And category TODOs with the following details exist
            | title                 | doneStatus | description          |
            | Finish lab report     | false      | due Friday           |
            | Prepare presentation  | false      | slides + rehearsal   |
            | Read chapter 3        | false      | notes for seminar    |

    # ------------------------- Normal Flow -------------------------
    Scenario Outline: List TODOs for a category that has linked TODOs (Normal Flow)
        Given the category "<category>" has the TODO "<todo1>" linked
        And the category "<category>" has the TODO "<todo2>" linked
        When the user requests all TODOs for the category "<category>"
        Then the response is successful
        And the response includes the TODO "<todo1>"
        And the response includes the TODO "<todo2>"

        Examples:
            | category       | todo1                | todo2                 |
            | High Priority  | Finish lab report    | Prepare presentation  |

    # ------------------------- Alternate Flow -------------------------
    Scenario: List TODOs for a category that currently has no TODOs (Alternate Flow)
        Given the category "Backlog" exists and has no linked TODOs
        When the user requests all TODOs for the category "Backlog"
        Then the response is successful
        And the response contains no TODOs

    # ------------------------- Error Flow -------------------------
    Scenario Outline: Request TODOs for a non-existent category (Error Flow)
        Given that there is no category with "<non_existing_category_id>"
        When the user requests all TODOs for the category id "<non_existing_category_id>"
        Then the response indicates not found with message "<message>"

        Examples:
            | non_existing_category_id | message                                                    |
            | 999                      | Could not find parent thing for relationship categories/999/todos |ccl
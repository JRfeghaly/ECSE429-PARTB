@stories16to20
Feature: Story 18 – Link a TODO to a Category

    As a student,
    I assign a category to a TODO
    So that I can prioritize and organize my tasks.

    Background: Server is running, base TODOs and priority categories exist
        Given the server is running
        And TODOs with the following details exist in the system 1
            | title                   | doneStatus | description            |
            | Finish lab report       | false      | due this Friday        |
            | Prepare presentation    | false      | slides + rehearsal     |
            | Read chapter 3          | false      | notes for seminar      |
        And todo categories with the following details exist
            | title          | description          |
            | High Priority  | urgent within 48 hrs |
            | Medium Priority| due this week        |
            | Low Priority   | nice to have         |

    # ------------------------- Normal Flow -------------------------
    Scenario Outline: Link an existing category to an existing TODO (Normal Flow)
        When the student links the category with title <category> to the TODO with title <todo>
        Then the TODO with title <todo> is linked to the category with title <category>
        And the student is notified of the completion of the linking operation

        Examples:
            | todo                 | category        |
            | "Finish lab report"  | "High Priority" |
            | "Prepare presentation"| "Medium Priority" |

    # ------------------------- Alternate Flow -------------------------
    Scenario Outline: Attempt to relink a category already linked to a TODO (Alternate Flow)
        Given the category with title <category> is already linked to the TODO with title <todo>
        When the student links the category with title <category> to the TODO with title <todo>
        Then the student is notified of a conflict with the message <message>

        Examples:
            | todo                | category       | message                       |
            | "Read chapter 3"    | "Low Priority" | "Relationship already exists" |

    # ------------------------- Error Flow -------------------------
    Scenario Outline: Link a non-existing category to an existing TODO (Error Flow)
        Given a category with id <non_existing_category_id> does not exist
        And a TODO with title <todo> exists
        When the student links the category with id <non_existing_category_id> to the TODO with title <todo>
        Then the student is notified of the non-existence error with a message <message>

        Examples:
            | non_existing_category_id | todo                | message                                                       |
            | "999"                    | "Finish lab report" | "Could not find thing matching value for id in categories"    |
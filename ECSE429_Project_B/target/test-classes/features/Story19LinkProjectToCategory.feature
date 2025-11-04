@stories16to20
Feature: Story 19 – Link a Project to a Category

    As a project manager,
    I associate a project with a category
    So that I can classify and filter projects effectively.

    Background: Server is running, base projects and categories exist
        Given the server is running
        And projects with the following details exist
            | title              | completed | description        | active |
            | Research Portal    | false     | prototype phase    | true   |
            | Data Cleanup       | false     | ETL refactor       | true   |
        And project categories records with the following details exist
            | title           | description           |
            | In Progress     | active development    |
            | Backlog         | planned but not active|
            | Completed       | delivered             |

    # ------------------------- Normal Flow -------------------------
    Scenario Outline: Link an existing category to an existing project (Normal Flow)
        When the manager links the category with title <category> to the project with title <project>
        Then the project with title <project> is linked to the category with title <category>
        And the manager is notified of the completion of the linking operation

        Examples:
            | project           | category     |
            | "Research Portal" | "In Progress"|
            | "Data Cleanup"    | "Backlog"    |

    # ------------------------- Alternate Flow -------------------------
    Scenario Outline: Attempt to relink a category already linked to a project (Alternate Flow)
        Given the category with title <category> is already linked to the project with title <project>
        When the manager links the category with title <category> to the project with title <project>
        Then the manager is notified of a conflict with the message <message>

        Examples:
            | project           | category   | message                       |
            | "Research Portal" | "Backlog"  | "Relationship already exists" |

    # ------------------------- Error Flow -------------------------
    Scenario Outline: Link a non-existing category to an existing project (Error Flow)
        Given no category with id <non_existing_category_id> exists
        And a project with title <project> exists
        When the manager links the category with id <non_existing_category_id> to the project with title <project>
        Then the manager is notified of the non-existence error with a message <message>

        Examples:
            | non_existing_category_id | project           | message                                                       |
            | "999"                    | "Research Portal" | "Could not find thing matching value for id in categories"    |
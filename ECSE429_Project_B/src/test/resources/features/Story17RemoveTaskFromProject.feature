@stories16to20
Feature: Story 17 – Remove a Task from a Project

    As a project manager,
    I want to remove a task from a project
    So that I can keep the project task list accurate and up to date.

    Background: Server is running, projects and TODOs are available
        Given the server is running
        And projects with the following details exist
            | title            | completed | description      | active |
            | Mobile App       | false     | UI polish phase  | true   |
            | Website Redesign | false     | UX improvement   | true   |
        And TODOs with the following details exist
            | title                | doneStatus | description          |
            | Implement dark mode  | false      | finish remaining UI  |
            | Fix navigation bugs  | false      | menu and back button |

    Scenario Outline: Successfully remove a task from a project (Normal Flow)
        Given the project <project> has the TODO <title> linked as a task
        When the project manager removes the TODO <title> from the project <project>
        Then the relationship between the TODO and the project is deleted successfully
        And the TODO no longer appears in the project’s task list

        Examples:
            | project           | title               |
            | "Mobile App"      | "Implement dark mode" |
            | "Website Redesign"| "Fix navigation bugs" |

    Scenario Outline: Attempt to remove a non-linked task from a project (Alternate Flow)
        Given the project <project> exists but the TODO <title> is not linked
        When the project manager tries to remove the TODO <title> from the project <project>
        Then the system returns a not found response with message <message>

        Examples:
            | project           | title                | message                                                          |
            | "Website Redesign"| "Implement dark mode" | "Could not find relationship projects/ID/tasks/TODO_ID to delete" |

    Scenario Outline: Attempt to remove a task from a non-existent project (Error Flow)
        Given the project with id <non_existing_project_id> does not exist
        When the project manager tries to remove a TODO with id <todo_id> from it
        Then the API returns a not found response with message <message>

        Examples:
            | non_existing_project_id | todo_id | message                                                          |
            | "500"                   | "999"   | "Could not find parent thing for relationship projects/500/tasks" |

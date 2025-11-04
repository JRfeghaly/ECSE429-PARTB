@stories16to20
Feature: Story 16 – Create a Task and Add it to a Project

    As a project manager,
    I want to create or link a task to a project
    So that I can organize and track project deliverables effectively.

    Background: Server is running, existing projects and TODOs are available
        Given the server is running
        And projects with the following details exist in 16
            | title             | completed | description       | active |
            | Website Redesign  | false     | UI improvements   | true   |
            | Marketing Launch  | false     | Campaign rollout  | true   |
        And TODOs with the following details exist in 16
            | title                 | doneStatus | description             |
            | Create landing page   | false      | design initial layout   |
            | Schedule social posts | false      | for next 2 weeks        |
            | Write product specs   | false      | version 1.0 draft       |

    Scenario Outline: Successfully add an existing task to a project (Normal Flow)
        When the project manager adds a TODO with title <title> to a project with name <project>
        Then the TODO with title <title> is linked as a task under the project named <project>
        And the project manager receives a confirmation of successful linking

        Examples:
            | title                   | doneStatus | description           | project            |
            | "Create landing page"   | false      | design initial layout | "Website Redesign" |
            | "Schedule social posts" | false      | for next 2 weeks      | "Marketing Launch" |

    Scenario Outline: Add a task after creating a new project (Alternative Flow)
        Given the project manager creates a new project with name <project> and description <description>
        When the project manager adds a TODO with title <title> to the project named <project>
        Then the TODO with title <title> is linked as a task under the project named <project>
        And the project manager receives a confirmation of successful linking

        Examples:
            | title                | doneStatus | project           | description        |
            | "Write product specs"| false      | "API Integration" | "Backend updates"  |

    Scenario Outline: Attempt to add a non-existing task to a project (Error Flow)
        Given a TODO with id <non_existing_id> does not exist
        When the project manager adds a TODO with id <non_existing_id> to a project named <project>
        Then the project manager is notified of the error with the message <message>

        Examples:
            | non_existing_id | project            | message                                                         |
            | "99"            | "Website Redesign" | "Could not find parent thing for relationship projects/99/tasks" |
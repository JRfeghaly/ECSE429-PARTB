package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class Story16CreateTaskToProjectSteps {

    private static final String BASE_URL = "http://localhost:4567";
    private Response response;  // <== FIXED: shared across steps
    private int projectId;
    private int todoId;

    // ----------------- Helpers -----------------
    private int getProjectIdByTitle(String title) {
        Response res = RestAssured.get(BASE_URL + "/projects");
        assertEquals(200, res.statusCode(), "GET /projects must be 200");
        List<Map<String, Object>> projects = res.jsonPath().getList("projects");
        if (projects == null) throw new AssertionError("No 'projects' array in response");

        for (Map<String, Object> p : projects) {
            if (title.equals(p.get("title"))) {
                return Integer.parseInt(p.get("id").toString());
            }
        }
        throw new AssertionError("Project with title '" + title + "' not found.");
    }

    private int getTodoIdByTitle(String title) {
        Response res = RestAssured.get(BASE_URL + "/todos");
        assertEquals(200, res.statusCode(), "GET /todos must be 200");
        List<Map<String, Object>> todos = res.jsonPath().getList("todos");
        if (todos == null) throw new AssertionError("No 'todos' array in response");

        for (Map<String, Object> t : todos) {
            if (title.equals(t.get("title"))) {
                return Integer.parseInt(t.get("id").toString());
            }
        }
        throw new AssertionError("TODO with title '" + title + "' not found.");
    }

    private void ensureProjectExists(String title) {
        Response res = RestAssured.get(BASE_URL + "/projects");
        List<Map<String, Object>> projects = res.jsonPath().getList("projects");
        boolean exists = false;

        if (projects != null) {
            for (Map<String, Object> p : projects) {
                if (title.equals(p.get("title"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            Response create = RestAssured
                    .given()
                    .contentType("application/json")
                    .body(Map.of("title", title, "active", true, "completed", false))
                    .post(BASE_URL + "/projects");
            assertTrue(create.statusCode() == 201 || create.statusCode() == 200,
                    "Create project should be 201/200");
        }
    }

    private void ensureTodoExists(String title, String description) {
        Response res = RestAssured.get(BASE_URL + "/todos");
        List<Map<String, Object>> todos = res.jsonPath().getList("todos");
        boolean exists = false;

        if (todos != null) {
            for (Map<String, Object> t : todos) {
                if (title.equals(t.get("title"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            Response create = RestAssured
                    .given()
                    .contentType("application/json")
                    .body(Map.of("title", title, "doneStatus", false, "description", description))
                    .post(BASE_URL + "/todos");
            assertTrue(create.statusCode() == 201 || create.statusCode() == 200,
                    "Create todo should be 201/200");
        }
    }

    private Response linkTodoToProject(int todoId, int projectId) {
        return RestAssured.given()
                .contentType("application/json")
                .body(Map.of("id", String.valueOf(todoId)))
                .post(BASE_URL + "/projects/" + projectId + "/tasks");
    }

    // ----------------- Step Definitions -----------------

    @Given("projects with the following details exist in 16")
    public void projectsWithTheFollowingDetailsExist(io.cucumber.datatable.DataTable table) {
        table.asMaps().forEach(row -> ensureProjectExists(row.get("title")));
    }

    @Given("TODOs with the following details exist in 16")
    public void todosWithTheFollowingDetailsExist(io.cucumber.datatable.DataTable table) {
        table.asMaps().forEach(row -> ensureTodoExists(row.get("title"), row.get("description")));
    }

    // ✅ Fixed: use instance-level response
    @When("the project manager adds a TODO with title {string} to a project with name {string}")
    public void the_project_manager_adds_a_todo_with_title_to_a_project_with_name(String todoTitle, String projectTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        todoId = getTodoIdByTitle(todoTitle);

        assertEquals(200, RestAssured.get(BASE_URL + "/projects/" + projectId).statusCode());
        assertEquals(200, RestAssured.get(BASE_URL + "/todos/" + todoId).statusCode());

        response = linkTodoToProject(todoId, projectId);
        assertTrue(response.statusCode() == 201 || response.statusCode() == 409,
                "Expected 201 or 409, got " + response.statusCode());
    }

    @Then("the TODO with title {string} is linked as a task under the project named {string}")
    public void the_todo_with_title_is_linked_as_a_task_under_the_project_named(String todoTitle, String projectTitle) {
        Response projectTasks = RestAssured.get(BASE_URL + "/projects/" + projectId + "/tasks");
        assertTrue(projectTasks.asString().contains(todoTitle),
                "Expected to find TODO '" + todoTitle + "' in project '" + projectTitle + "' task list.");
    }

    @Given("the project manager creates a new project with name {string} and description {string}")
    public void the_project_manager_creates_a_new_project_with_name_and_description(String name, String description) {
        Response create = RestAssured.given()
                .contentType("application/json")
                .body(Map.of("title", name, "description", description, "active", true, "completed", false))
                .post(BASE_URL + "/projects");
        assertTrue(create.statusCode() == 200 || create.statusCode() == 201,
                "Create project should return 200/201");
    }

    // ✅ Fixed: use class-level response here too
    @When("the project manager adds a TODO with title {string} to the project named {string}")
    public void the_project_manager_adds_a_todo_with_title_to_the_project_named(String todoTitle, String projectTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        todoId = getTodoIdByTitle(todoTitle);

        response = linkTodoToProject(todoId, projectId);
        assertTrue(response.statusCode() == 201 || response.statusCode() == 409,
                "Expected 201 or 409, got " + response.statusCode());
    }

    @Then("the project manager receives a confirmation of successful linking")
    public void the_project_manager_receives_a_confirmation_of_successful_linking() {
        assertNotNull(response, "Response should not be null — missing assignment in @When step");
        assertEquals(201, response.statusCode(),
                "Expected 201 Created, got " + response.statusCode() + " with body: " + response.asString());
    }

    @Given("a TODO with id {string} does not exist")
    public void a_todo_with_id_does_not_exist(String id) {
        int tid = Integer.parseInt(id);
        Response res = RestAssured.get(BASE_URL + "/todos/" + tid);
        assertEquals(404, res.statusCode(), "Expected TODO not to exist");
    }

    // ✅ Fixed: store response
    @When("the project manager adds a TODO with id {string} to a project named {string}")
    public void the_project_manager_adds_a_todo_with_id_to_a_project_named(String todoIdStr, String projectTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        int tid = Integer.parseInt(todoIdStr);

        response = linkTodoToProject(tid, projectId);
        assertTrue(response.statusCode() == 404 || response.statusCode() == 400,
                "Expected 404 or 400 for missing TODO, got " + response.statusCode());
    }

    @Then("the project manager is notified of the error with the message {string}")
    public void the_project_manager_is_notified_of_the_error_with_the_message(String message) {
        assertEquals(404, response.statusCode(), "Expected 404 Not Found for missing TODO.");
        String body = response.asString();
        assertTrue(
            body.contains("Could not find thing matching value for id") ||
            body.contains("Could not find parent thing for relationship") ||
            body.contains(message),
            "Expected message not found in response. Actual: " + body
        );
    }
}
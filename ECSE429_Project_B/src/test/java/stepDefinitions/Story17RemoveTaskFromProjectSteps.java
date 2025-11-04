package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class Story17RemoveTaskFromProjectSteps {

    private static final String BASE_URL = "http://localhost:4567";
    private int projectId;
    private int todoId;
    private Response deleteResponse;

    // ---------- Helpers ----------
    private int getProjectIdByTitle(String title) {
        Response res = RestAssured.get(BASE_URL + "/projects");
        assertEquals(200, res.statusCode(), "GET /projects must be 200");
        List<Map<String, Object>> projects = res.jsonPath().getList("projects");
        for (Map<String, Object> p : projects) {
            if (title.equals(p.get("title"))) {
                return Integer.parseInt(p.get("id").toString());
            }
        }
        throw new AssertionError("Project not found: " + title);
    }

    private int getTodoIdByTitle(String title) {
        Response res = RestAssured.get(BASE_URL + "/todos");
        assertEquals(200, res.statusCode(), "GET /todos must be 200");
        List<Map<String, Object>> todos = res.jsonPath().getList("todos");
        for (Map<String, Object> t : todos) {
            if (title.equals(t.get("title"))) {
                return Integer.parseInt(t.get("id").toString());
            }
        }
        throw new AssertionError("TODO not found: " + title);
    }

    private Response createTodo(String title, boolean doneStatus) {
        Response response = RestAssured.given()
                .contentType("application/json")
                .body(Map.of(
                        "title", title,
                        "doneStatus", doneStatus,
                        "description", "Auto-created for test"
                ))
                .post(BASE_URL + "/todos");

        assertTrue(response.statusCode() == 200 || response.statusCode() == 201 || response.statusCode() == 409,
                "Expected 200/201/409, got " + response.statusCode());
        return response;
    }

    private void linkIfNeeded(int pid, int tid) {
        Response list = RestAssured.get(BASE_URL + "/projects/" + pid + "/tasks");
        if (list.statusCode() == 200) {
            List<Map<String, Object>> tasks = list.jsonPath().getList("todos");
            boolean linked = false;
            if (tasks != null) {
                for (Map<String, Object> t : tasks) {
                    if (Integer.parseInt(t.get("id").toString()) == tid) {
                        linked = true;
                        break;
                    }
                }
            }
            if (!linked) {
                Response link = RestAssured.given()
                        .contentType("application/json")
                        .body(Map.of("id", Integer.toString(tid)))
                        .post(BASE_URL + "/projects/" + pid + "/tasks");
                assertTrue(link.statusCode() == 201 || link.statusCode() == 409,
                        "Expected 201 or 409 when linking, got " + link.statusCode());
            }
        }
    }

    // ---------- Step glue ----------
    @Given("the project {string} has the TODO {string} linked as a task")
    public void projectHasTodoLinked(String projectTitle, String todoTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        todoId = getTodoIdByTitle(todoTitle);
        assertEquals(200, RestAssured.get(BASE_URL + "/projects/" + projectId).statusCode());
        assertEquals(200, RestAssured.get(BASE_URL + "/todos/" + todoId).statusCode());
        linkIfNeeded(projectId, todoId);
    }

    @When("the project manager removes the TODO {string} from the project {string}")
    public void removeTodoFromProject(String todoTitle, String projectTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        todoId = getTodoIdByTitle(todoTitle);
        deleteResponse = RestAssured.delete(BASE_URL + "/projects/" + projectId + "/tasks/" + todoId);
    }

    @Then("the relationship between the TODO and the project is deleted successfully")
    public void verifyTaskUnlinked() {
        assertEquals(200, deleteResponse.statusCode(), "Expected 200 OK for successful deletion");

        Response list = RestAssured.get(BASE_URL + "/projects/" + projectId + "/tasks");
        assertEquals(200, list.statusCode());
        List<Map<String, Object>> tasks = list.jsonPath().getList("todos");
        boolean stillThere = false;
        if (tasks != null) {
            for (Map<String, Object> t : tasks) {
                if (Integer.parseInt(t.get("id").toString()) == todoId) {
                    stillThere = true;
                    break;
                }
            }
        }
        assertFalse(stillThere, "TODO still appears under project after deletion");
    }

    @Given("the project {string} exists but the TODO {string} is not linked")
    public void projectExistsButTodoNotLinked(String projectTitle, String todoTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        todoId = getTodoIdByTitle(todoTitle);
        // Ensure NOT linked: attempt deletion and ignore 404
        RestAssured.delete(BASE_URL + "/projects/" + projectId + "/tasks/" + todoId);
    }

    @When("the project manager tries to remove the TODO {string} from the project {string}")
    public void removeUnlinkedTodo(String todoTitle, String projectTitle) {
        projectId = getProjectIdByTitle(projectTitle);
        todoId = getTodoIdByTitle(todoTitle);
        deleteResponse = RestAssured.delete(BASE_URL + "/projects/" + projectId + "/tasks/" + todoId);
    }

    @Then("the system returns a not found response with message {string}")
    public void verifyNotFoundResponse(String expectedMessage) {
        assertEquals(404, deleteResponse.statusCode(), "Expected 404 when relationship does not exist");

        String body = deleteResponse.asString();
        assertTrue(
                body.contains("Could not find any instances with") ||
                body.contains("Could not find parent thing") ||
                body.contains("Could not find relationship") ||
                body.contains(expectedMessage),
                "Expected 'not found' message missing. Actual body: " + body
        );
    }

    @Given("the project with id {string} does not exist")
    public void projectDoesNotExist(String pidStr) {
        int pid = Integer.parseInt(pidStr);
        Response res = RestAssured.get(BASE_URL + "/projects/" + pid);
        assertEquals(404, res.statusCode(), "Expected project not to exist");
    }

    @When("the project manager tries to remove a TODO with id {string} from it")
    public void removeTodoFromNonexistentProject(String tidStr) {
        int tid = Integer.parseInt(tidStr);
        deleteResponse = RestAssured.delete(BASE_URL + "/projects/500/tasks/" + tid);
    }

    @Then("the API returns a not found response with message {string}")
    public void verifyNotFoundErrorMessage(String expectedMessage) {
        assertEquals(404, deleteResponse.statusCode(), "Expected 404 for non-existent project");
        String body = deleteResponse.asString();
        assertTrue(
                body.contains("Could not find any instances with") ||
                body.contains("Could not find parent thing") ||
                body.contains(expectedMessage),
                "Expected 'not found' message missing. Actual body: " + body
        );
    }

    @Given("TODOs with the following details exist in 17")
    public void todos_with_the_following_details_exist(io.cucumber.datatable.DataTable dataTable) {
        List<Map<String, String>> todos = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> todo : todos) {
            createTodo(todo.get("title"), Boolean.parseBoolean(todo.get("doneStatus")));
        }
    }

    @Then("the TODO no longer appears in the project’s task list")
    public void the_todo_no_longer_appears_in_the_project_s_task_list() {
        Response projectTasks = RestAssured.get(BASE_URL + "/projects/" + projectId + "/tasks");
        assertFalse(projectTasks.asString().contains(String.valueOf(todoId)),
                "TODO " + todoId + " should not appear in project " + projectId + " tasks list.");
    }
}
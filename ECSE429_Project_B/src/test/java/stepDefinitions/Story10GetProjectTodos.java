package stepDefinitions;

import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Story10GetProjectTodos {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;

    /* -------------------- Background -------------------- */
    @Given("the server is running for Story10")
    public void serverIsRunning() {
        RestAssured.get(BASE_URL + "/projects").then().statusCode(200);
    }

    @Given("a project with id {int} exists")
    public void projectExists(int id) {
        JSONObject body = new JSONObject();
        body.put("title", "Project " + id);
        body.put("completed", false);
        body.put("description", "Test project");
        body.put("active", true);

        Response postResponse = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/projects");

        assertTrue(postResponse.statusCode() == 200 || postResponse.statusCode() == 201,
                "Failed to create project with id " + id);
    }

    @Given("the project has the following tasks")
    public void projectHasTasks(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            JSONObject task = new JSONObject();
            task.put("title", row.get("title"));
            task.put("completed", Boolean.parseBoolean(row.get("completed")));

            Response postTask = RestAssured.given()
                    .contentType("application/json")
                    .body(task.toString())
                    .post(BASE_URL + "/projects/1/tasks");

            assertTrue(postTask.statusCode() == 200 || postTask.statusCode() == 201,
                    "Failed to create task " + row.get("title"));
        }
    }

    /* -------------------- Normal Flow -------------------- */
    @When("the user requests tasks for project with id {int}")
    public void requestTasksForProject(int id) {
        response = RestAssured.get(BASE_URL + "/projects/" + id + "/tasks");
        System.out.println("Server response: " + response.asString());
    }

    @Then("the server should respond with status {int}")
    public void serverShouldRespondWithStatus(int expectedStatus) {
        int actualStatus = response.statusCode();
        assertEquals(expectedStatus, actualStatus,
                "Expected status " + expectedStatus + " but got " + actualStatus);
    }

    @Then("the user should receive the following tasks")
    public void userShouldReceiveTasks(DataTable dataTable) {
        JSONArray tasks = new JSONArray(response.asString());
        List<Map<String, String>> expectedTasks = dataTable.asMaps(String.class, String.class);
        assertEquals(expectedTasks.size(), tasks.length(), "Number of tasks does not match");

        for (int i = 0; i < tasks.length(); i++) {
            JSONObject actualTask = tasks.getJSONObject(i);
            Map<String, String> expectedTask = expectedTasks.get(i);
            assertEquals(expectedTask.get("title"), actualTask.getString("title"));
            assertEquals(Boolean.parseBoolean(expectedTask.get("completed")), actualTask.getBoolean("completed"));
        }
    }

    /* -------------------- Error Flow -------------------- */
    @Given("a project with id {int} does not exist")
    public void projectDoesNotExist(int id) {
        Response get = RestAssured.get(BASE_URL + "/projects/" + id);
        assertEquals(404, get.statusCode(), "Project " + id + " should not exist before test");
    }

    @Then("the user should not receive any tasks")
    public void userShouldNotReceiveAnyTasks() {
        String body = response.asString();
        assertTrue(body.isEmpty() || body.equals("[]"),
                "Expected no tasks for nonexistent project, but got: " + body);
    }
}

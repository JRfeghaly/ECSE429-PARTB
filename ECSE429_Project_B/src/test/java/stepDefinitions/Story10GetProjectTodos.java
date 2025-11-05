package stepDefinitions;

import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class Story10GetProjectTodos {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private int createdProjectId;
    private final Map<Integer, Integer> featureToActual = new HashMap<>();

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
        
        // Store the actual ID returned by the server
        JSONObject responseBody = new JSONObject(postResponse.asString());
        int actualId = Integer.parseInt(responseBody.getString("id"));
        createdProjectId = actualId;
        // Map the logical feature id (e.g., 1) to the actual server id returned
        featureToActual.put(id, actualId);
    }

    @Given("the project has the following tasks")
    public void projectHasTasks(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            String title = row.get("title").replaceAll("^\"|\"$", "");
            boolean completed = Boolean.parseBoolean(row.get("completed"));
            
            // First create the TODO
            JSONObject task = new JSONObject();
            task.put("title", title);
            task.put("doneStatus", completed);

            Response createTodoResponse = RestAssured.given()
                    .contentType("application/json")
                    .body(task.toString())
                    .post(BASE_URL + "/todos");

            assertTrue(createTodoResponse.statusCode() == 200 || createTodoResponse.statusCode() == 201,
                    "Failed to create TODO: " + title);
            
            // Get the TODO ID from response
            JSONObject todoResponse = new JSONObject(createTodoResponse.asString());
            String todoId = todoResponse.getString("id");
            
            // Then link it to the project
            JSONObject linkBody = new JSONObject();
            linkBody.put("id", todoId);
            
            Response linkResponse = RestAssured.given()
                    .contentType("application/json")
                    .body(linkBody.toString())
                    .post(BASE_URL + "/projects/" + createdProjectId + "/tasks");

            assertTrue(linkResponse.statusCode() == 200 || linkResponse.statusCode() == 201,
                    "Failed to link task: " + title);
        }
    }

    /* -------------------- Normal Flow -------------------- */
    @When("the user requests tasks for project with id {int}")
    public void requestTasksForProject(int id) {
        int actualId = featureToActual.getOrDefault(id, id);
        response = RestAssured.get(BASE_URL + "/projects/" + actualId + "/tasks");
        System.out.println("Server response for project id " + actualId + ": " + response.asString());
    }

    @Then("the server should respond with status {int}")
    public void serverShouldRespondWithStatus(int expectedStatus) {
        int actualStatus = response.statusCode();
        assertEquals(expectedStatus, actualStatus,
                "Expected status " + expectedStatus + " but got " + actualStatus);
    }

    @Then("the server should respond with an error status {int}")
    public void serverShouldRespondWithErrorStatus(int expectedStatus) {
        int actualStatus = response.statusCode();
        // If the server returns 200 when an error is expected, report this as a BUG (server is returning success for a missing project)
        if (actualStatus == 200 && expectedStatus != 200) {
            fail("BUG: Expected error status " + expectedStatus + " but server returned 200. Response body: " + response.asString());
        }

        assertEquals(expectedStatus, actualStatus,
                "Expected error status " + expectedStatus + " but got " + actualStatus);
    }

    @Then("the user should receive the following tasks")
    public void userShouldReceiveTasks(DataTable dataTable) {
        JSONObject responseObj = new JSONObject(response.asString());
        JSONArray tasks = responseObj.getJSONArray("todos");
        List<Map<String, String>> expectedTasks = dataTable.asMaps(String.class, String.class);
        assertEquals(expectedTasks.size(), tasks.length(), "Number of tasks does not match");

        // Check that all expected tasks are present (order-independent)
        for (Map<String, String> expectedTask : expectedTasks) {
            String expectedTitle = expectedTask.get("title").replaceAll("^\"|\"$", "");
            String expectedCompleted = expectedTask.get("completed");
            
            boolean found = false;
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject actualTask = tasks.getJSONObject(i);
                if (actualTask.getString("title").equals(expectedTitle) &&
                    actualTask.getString("doneStatus").equals(expectedCompleted)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Expected task not found: " + expectedTitle + " with completed=" + expectedCompleted);
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
        int status = response.statusCode();
        // If server returned an error status (e.g., 404), treat as no tasks for the error-flow
        if (status != 200) return;

        String body = response.asString();
        // If status is 200, ensure the tasks list is empty
        try {
            JSONObject responseObj = new JSONObject(body);
            if (responseObj.has("todos")) {
                assertEquals(0, responseObj.getJSONArray("todos").length(),
                        "Expected no tasks for nonexistent project, but got: " + body);
                return;
            }
        } catch (Exception e) {
        }

        assertTrue(body.isEmpty() || body.equals("[]"),
                "Expected no tasks for nonexistent project, but got: " + body);
    }
}

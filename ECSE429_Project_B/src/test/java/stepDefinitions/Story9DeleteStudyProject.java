package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Story9DeleteStudyProject {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private final Map<Integer, String> projectIds = new HashMap<>();
    private final List<String> createdIds = new ArrayList<>();

    /* -------------------- Background -------------------- */
    @Given("the server is running for permanent delete")
    public void serverIsRunning() {
        RestAssured.get(BASE_URL + "/projects").then().statusCode(200);
    }

    @Given("the following projects exist for permanent delete")
    public void projectsExist(DataTable dataTable) {
        // Clean up all existing projects
        Response getResponse = RestAssured.get(BASE_URL + "/projects");
        if (getResponse.statusCode() == 200 && getResponse.jsonPath().getList("projects") != null) {
            var existingIds = getResponse.jsonPath().getList("projects.id");
            for (Object id : existingIds) {
                RestAssured.delete(BASE_URL + "/projects/" + id);
            }
        }

        dataTable.asMaps().forEach(row -> {
            Integer featureId = Integer.parseInt(row.get("id"));
            String title = row.get("title").replace("\"", "");
            boolean completed = Boolean.parseBoolean(row.get("completed"));
            String description = row.get("description").replace("\"", "");
            boolean active = Boolean.parseBoolean(row.get("active"));

            JSONObject body = new JSONObject()
                    .put("title", title)
                    .put("completed", completed)
                    .put("description", description)
                    .put("active", active);

            Response postResponse = RestAssured.given()
                    .contentType("application/json")
                    .body(body.toString())
                    .post(BASE_URL + "/projects");

            assertTrue(postResponse.statusCode() == 200 || postResponse.statusCode() == 201,
                    "Failed to create test project: " + title);

            String actualId = postResponse.jsonPath().getString("id");
            if (actualId != null) {
                createdIds.add(actualId);
                projectIds.put(featureId, actualId);
            }
        });
    }

    /* -------------------- Normal Flow -------------------- */
    @Given("a user has a project with permanent delete ID {int}")
    public void userHasProject(int id) {
        String actual = projectIds.getOrDefault(id, String.valueOf(id));
        Response get = RestAssured.get(BASE_URL + "/projects/" + actual);
        assertEquals(200, get.statusCode(), "Expected project with ID " + id + " to exist");
    }

    @When("the user requests to permanently delete the project with permanent delete ID {int}")
    public void userDeletesProject(int id) {
        String actualId = projectIds.getOrDefault(id, String.valueOf(id));
        response = RestAssured.delete(BASE_URL + "/projects/" + actualId);
    }

    @Then("the project with permanent delete ID {int} is removed from the project list")
    public void projectIsRemoved(int id) {
        String actual = projectIds.getOrDefault(id, String.valueOf(id));
        Response get = RestAssured.get(BASE_URL + "/projects/" + actual);
        assertEquals(404, get.statusCode(), "Expected project to be deleted");
    }

    @Then("the user is notified of the successful deletion")
    public void userNotifiedSuccess() {
        assertNotNull(response);
        int status = response.statusCode();
        assertTrue(status == 200 || status == 201, "Expected 200/201 for successful delete, got " + status);
    }

    /* -------------------- Error Flow -------------------- */
    @Given("a project with permanent delete ID {int} does not exist")
    public void projectDoesNotExist(int id) {
        String idStr = String.valueOf(id);
        Response get = RestAssured.get(BASE_URL + "/projects/" + idStr);
        assertEquals(404, get.statusCode(), "Project should not exist");
    }

    @Then("the user is notified with an error message {string}")
    public void userReceivesErrorMessage(String message) {
        String body = response.asString();
        assertTrue(body.contains(message),
                "Expected error message not found: " + body);
    }

    /* -------------------- Cleanup -------------------- */
    @After
    public void cleanupCreatedProjects() {
        for (String id : new ArrayList<>(createdIds)) {
            try {
                RestAssured.delete(BASE_URL + "/projects/" + id);
            } catch (Exception ignored) { }
        }
        createdIds.clear();
        projectIds.clear();
    }
}

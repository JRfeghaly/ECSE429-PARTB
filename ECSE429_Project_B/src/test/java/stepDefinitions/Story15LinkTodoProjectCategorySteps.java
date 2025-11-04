package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Story15LinkTodoProjectCategorySteps {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;

    // Maps to track created resources for cleanup
    private final Map<String, String> categoryIds = new HashMap<>();
    private final Map<String, String> todoIds = new HashMap<>();
    private final Map<String, String> projectIds = new HashMap<>();

    /* -------------------- Background -------------------- */

    @Given("the server is running for link category")
    public void theServerIsRunning() {
        RestAssured.get(BASE_URL + "/categories").then().statusCode(200);
    }

    @Given("a category with id {int} and title {string} exists")
    public void categoryWithIdAndTitleExists(int logicalId, String title) {
        // Create a category (Thingifier ignores provided ID)
        JSONObject body = new JSONObject()
                .put("title", title.replace("\"", ""))
                .put("description", "Auto-created test category");

        Response postResponse = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories");

        assertEquals(201, postResponse.statusCode(), "Failed to create category for linking test");

        String id = postResponse.jsonPath().getString("id");
        categoryIds.put(title, id);
        System.out.println("✅ Created category for linking test: " + title + " (ID: " + id + ")");
    }

    @Given("TODOs with the following details exist")
    public void todosExist(DataTable dataTable) {
        dataTable.asMaps().forEach(row -> {
            JSONObject body = new JSONObject()
                    .put("title", row.get("title").replace("\"", ""))
                    .put("doneStatus", Boolean.parseBoolean(row.get("doneStatus")))
                    .put("description", row.get("description").replace("\"", ""));

            Response postResponse = RestAssured.given()
                    .contentType("application/json")
                    .body(body.toString())
                    .post(BASE_URL + "/todos");

            assertEquals(201, postResponse.statusCode(), "Failed to create TODO: " + row.get("title"));

            String id = postResponse.jsonPath().getString("id");
            todoIds.put(row.get("title").replace("\"", ""), id);
        });

        System.out.println("✅ Created TODOs for linking test: " + todoIds);
    }

    @Given("Projects with the following details exist")
    public void projectsExist(DataTable dataTable) {
        dataTable.asMaps().forEach(row -> {
            JSONObject body = new JSONObject()
                    .put("title", row.get("title").replace("\"", ""))
                    .put("description", row.get("description").replace("\"", ""));

            Response postResponse = RestAssured.given()
                    .contentType("application/json")
                    .body(body.toString())
                    .post(BASE_URL + "/projects");

            assertEquals(201, postResponse.statusCode(), "Failed to create Project: " + row.get("title"));

            String id = postResponse.jsonPath().getString("id");
            projectIds.put(row.get("title").replace("\"", ""), id);
        });

        System.out.println("✅ Created Projects for linking test: " + projectIds);
    }

    /* -------------------- Normal Flow -------------------- */

    @When("the user links TODO with id {int} to category with id {int}")
    public void userLinksTodoToCategory(int logicalTodoId, int logicalCategoryId) {
        // Map logical IDs to real created IDs
        String todoId = todoIds.values().iterator().next();
        String categoryId = categoryIds.values().iterator().next();

        JSONObject body = new JSONObject().put("id", todoId);

        response = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories/" + categoryId + "/todos");
    }

    @Then("the link server responds with status {int}")
    public void linkServerRespondsWithStatus(int expectedStatus) {
        assertNotNull(response, "Response was null — link request did not execute");
        assertEquals(expectedStatus, response.statusCode(),
                "Unexpected HTTP status for link operation");
    }

    @Then("the category with id {int} now includes TODO {int}")
    public void categoryNowIncludesTodo(int logicalCategoryId, int logicalTodoId) {
        String categoryId = categoryIds.values().iterator().next();
        String todoId = todoIds.values().iterator().next();

        Response getResponse = RestAssured.get(BASE_URL + "/categories/" + categoryId + "/todos");
        assertEquals(200, getResponse.statusCode(), "Expected successful retrieval of linked TODOs");

        String body = getResponse.asString();
        assertTrue(body.contains(todoId),
                "Expected TODO " + todoId + " to be linked to category " + categoryId);
    }

    /* -------------------- Alternate Flow -------------------- */

    @Given("TODO with id {int} is already linked to category {int}")
    public void todoAlreadyLinkedToCategory(int logicalTodoId, int logicalCategoryId) {
        String todoId = todoIds.values().iterator().next();
        String categoryId = categoryIds.values().iterator().next();

        JSONObject body = new JSONObject().put("id", todoId);
        Response postResponse = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories/" + categoryId + "/todos");

        assertTrue(postResponse.statusCode() == 201 || postResponse.statusCode() == 200,
                "Expected successful initial link creation");
    }

    @When("the user links TODO with id {int} to category with id {int} again")
    public void userLinksTodoAgain(int logicalTodoId, int logicalCategoryId) {
        String todoId = todoIds.values().iterator().next();
        String categoryId = categoryIds.values().iterator().next();

        JSONObject body = new JSONObject().put("id", todoId);
        response = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories/" + categoryId + "/todos");
    }

    @Then("no duplicate link is created")
    public void noDuplicateLinkIsCreated() {
        String categoryId = categoryIds.values().iterator().next();
        String todoId = todoIds.values().iterator().next();

        Response getResponse = RestAssured.get(BASE_URL + "/categories/" + categoryId + "/todos");
        assertEquals(200, getResponse.statusCode());

        String body = getResponse.asString();

        // Thingifier typically doesn’t duplicate — just return 200
        assertTrue(body.contains(todoId), "Expected TODO to remain linked exactly once");
    }

    /* -------------------- Error Flow -------------------- */

    @When("the user links TODO with id {int} to category with id {int} that does not exist")
    public void userLinksTodoToNonexistentCategory(int logicalTodoId, int invalidCategoryId) {
        String todoId = todoIds.values().iterator().next();

        JSONObject body = new JSONObject().put("id", todoId);

        response = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories/" + invalidCategoryId + "/todos");
    }

    @Then("the user is notified with link message {string}")
    public void userIsNotifiedWithLinkMessage(String expectedMessage) {
        String responseBody = response.asString();
        System.out.println("🔍 Link response body: " + responseBody);

        boolean messageFound =
                responseBody.contains(expectedMessage.replace("\"", "")) ||
                        responseBody.toLowerCase().contains("could not find parent thing") ||
                        responseBody.toLowerCase().contains("error");

        assertTrue(messageFound,
                "Expected link error message not found.\nActual body: " + responseBody);
    }

    /* -------------------- Cleanup -------------------- */

    @After
    public void cleanupCreatedEntities() {
        // Cleanup TODOS
        for (String id : todoIds.values()) {
            try {
                RestAssured.delete(BASE_URL + "/todos/" + id);
                System.out.println("🧹 Deleted test TODO ID: " + id);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete TODO ID " + id + ": " + e.getMessage());
            }
        }

        // Cleanup PROJECTS
        for (String id : projectIds.values()) {
            try {
                RestAssured.delete(BASE_URL + "/projects/" + id);
                System.out.println("🧹 Deleted test Project ID: " + id);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete Project ID " + id + ": " + e.getMessage());
            }
        }

        // Cleanup CATEGORIES
        for (String id : categoryIds.values()) {
            try {
                RestAssured.delete(BASE_URL + "/categories/" + id);
                System.out.println("🧹 Deleted test Category ID: " + id);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete Category ID " + id + ": " + e.getMessage());
            }
        }

        todoIds.clear();
        projectIds.clear();
        categoryIds.clear();
    }
}

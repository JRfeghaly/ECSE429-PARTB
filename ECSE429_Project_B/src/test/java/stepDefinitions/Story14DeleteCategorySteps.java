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

public class Story14DeleteCategorySteps {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private final Map<String, String> categoryIds = new HashMap<>();

    /* -------------------- Background -------------------- */

    @Given("the server is running for delete category")
    public void theServerIsRunning() {
        RestAssured.get(BASE_URL + "/categories")
                .then()
                .statusCode(200);
    }

    @Given("categories with the following details exist for delete category")
    public void categoriesExist(DataTable dataTable) {
        Response getResponse = RestAssured.get(BASE_URL + "/categories");
        if (getResponse.statusCode() == 200 && getResponse.jsonPath().getList("categories") != null) {
            var existingIds = getResponse.jsonPath().getList("categories.id");
            for (Object id : existingIds) {
                RestAssured.delete(BASE_URL + "/categories/" + id);
            }
        }

        dataTable.asMaps().forEach(row -> {
            String title = row.get("title").replace("\"", "");
            String description = row.get("description").replace("\"", "");

            JSONObject body = new JSONObject()
                    .put("title", title)
                    .put("description", description);

            Response postResponse = RestAssured.given()
                    .contentType("application/json")
                    .body(body.toString())
                    .post(BASE_URL + "/categories");

            assertEquals(201, postResponse.statusCode(),
                    "Failed to create category for delete test: " + title);

            String id = postResponse.jsonPath().getString("id");
            categoryIds.put(title, id);
        });

        System.out.println("✅ Created test categories for deletion: " + categoryIds);
    }

    /* -------------------- Normal Flow -------------------- */

    @When("the user deletes category with id {int}")
    public void userDeletesCategoryWithId(int logicalId) {
        String existingId = categoryIds.values().iterator().next();
        response = RestAssured.delete(BASE_URL + "/categories/" + existingId);
    }

    @Then("the delete server responds with status {int}")
    public void deleteServerRespondsWithStatus(int expectedStatus) {
        assertNotNull(response, "Response was null — delete operation did not execute");
        assertEquals(expectedStatus, response.statusCode(),
                "Unexpected HTTP status from DELETE /categories/:id");
    }

    @Then("the category with id {int} should no longer exist")
    public void categoryShouldNoLongerExist(int logicalId) {
        String deletedId = categoryIds.values().iterator().next();
        Response getResponse = RestAssured.get(BASE_URL + "/categories/" + deletedId);
        assertEquals(404, getResponse.statusCode(),
                "Expected category to be deleted, but it still exists");
    }

    /* -------------------- Alternate Flow -------------------- */

    @Given("category with id {int} was deleted previously")
    public void categoryWasDeletedPreviously(int logicalId) {
        String existingId = categoryIds.values().iterator().next();
        Response deleteResponse = RestAssured.delete(BASE_URL + "/categories/" + existingId);
        assertEquals(200, deleteResponse.statusCode(),
                "Expected first delete to succeed before re-deletion test");
    }

    @When("the user deletes category with id {int} again")
    public void userDeletesCategoryAgain(int logicalId) {
        String existingId = categoryIds.values().iterator().next();
        response = RestAssured.delete(BASE_URL + "/categories/" + existingId);
    }

    @Then("the user is notified with delete message {string}")
    public void userIsNotifiedWithDeleteMessage(String expectedMessage) {
        String body = response.asString();
        System.out.println("🔍 Delete response body: " + body);

        // Thingifier returns "Could not find" or "errorMessages" array
        boolean messageFound =
                body.toLowerCase().contains("could not find") ||
                        body.toLowerCase().contains("error") ||
                        body.toLowerCase().contains("not found") ||
                        body.contains(expectedMessage.replace("\"", ""));

        assertTrue(messageFound,
                "Expected delete error message not found in response.\nActual body: " + body);
    }

    /* -------------------- Error Flow -------------------- */

    @When("the user deletes category with id {string}")
    public void userDeletesCategoryWithInvalidId(String invalidId) {
        response = RestAssured.delete(BASE_URL + "/categories/" + invalidId);
    }

    @Then("the user is notified with message {string} for invalid delete")
    public void userIsNotifiedWithInvalidDeleteMessage(String expectedMessage) {
        String responseBody = response.asString();
        System.out.println("🔍 Invalid delete response body: " + responseBody);

        boolean messageFound =
                responseBody.toLowerCase().contains("error") ||
                        responseBody.toLowerCase().contains("invalid") ||
                        responseBody.contains(expectedMessage.replace("\"", ""));

        assertTrue(messageFound,
                "Expected invalid delete message not found.\nActual body: " + responseBody);
    }

    /* -------------------- Cleanup -------------------- */

    @After
    public void cleanupCreatedCategories() {
        if (!categoryIds.isEmpty()) {
            for (String id : categoryIds.values()) {
                try {
                    RestAssured.delete(BASE_URL + "/categories/" + id);
                    System.out.println("🧹 Deleted test category ID: " + id);
                } catch (Exception e) {
                    System.err.println("⚠️ Failed to delete category ID " + id + ": " + e.getMessage());
                }
            }
            categoryIds.clear();
        }
    }
}

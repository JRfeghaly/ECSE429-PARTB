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

public class Story13UpdateCategorySteps {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private final Map<String, String> categoryIds = new HashMap<>(); // title → id
    private final Map<String, String> originalTitles = new HashMap<>(); // id → title (for verification)

    /* -------------------- Background -------------------- */

    @Given("the server is running for update category")
    public void theServerIsRunning() {
        RestAssured.get(BASE_URL + "/categories")
                .then()
                .statusCode(200);
    }

    @Given("categories with the following details exist for update category")
    public void categoriesExist(DataTable dataTable) {
        // 🧹 Clean up any pre-existing categories
        Response getResponse = RestAssured.get(BASE_URL + "/categories");
        if (getResponse.statusCode() == 200 && getResponse.jsonPath().getList("categories") != null) {
            var existingIds = getResponse.jsonPath().getList("categories.id");
            for (Object id : existingIds) {
                RestAssured.delete(BASE_URL + "/categories/" + id);
            }
        }

        // 🏗 Create categories needed for this test
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
                    "Failed to create category: " + title);

            String id = postResponse.jsonPath().getString("id");
            categoryIds.put(title, id);
            originalTitles.put(id, title);
        });

        System.out.println("✅ Created test categories: " + categoryIds);
    }

    /* -------------------- Normal Flow -------------------- */

    @When("the user updates category with id {int} to title {string} and description {string}")
    public void userUpdatesCategoryWithNewTitleAndDescription(int logicalId, String newTitle, String newDescription) {
        // Retrieve the actual API-generated category ID (not the logical one)
        String existingId = categoryIds.values().iterator().next();

        JSONObject body = new JSONObject()
                .put("title", newTitle.replace("\"", ""))
                .put("description", newDescription.replace("\"", ""));

        // Use PUT for a full update (API spec)
        response = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .put(BASE_URL + "/categories/" + existingId);
    }

    @Then("the server responds with status {int} for update")
    public void serverRespondsWithStatusForUpdate(int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(),
                "Unexpected HTTP status during update operation");
    }

    @Then("the category’s title is now {string}")
    public void categoryTitleIsNow(String expectedTitle) {
        String id = categoryIds.values().iterator().next();
        Response getResponse = RestAssured.get(BASE_URL + "/categories/" + id);

        JSONObject body = new JSONObject(getResponse.asString());
        JSONObject category = body.getJSONArray("categories").getJSONObject(0);

        assertEquals(expectedTitle.replace("\"", ""), category.getString("title"),
                "Title was not updated as expected");
    }

    /* -------------------- Alternate Flow -------------------- */

    @When("the user updates category with id {int} to description {string}")
    public void userUpdatesCategoryDescriptionOnly(int logicalId, String newDescription) {
        String existingId = categoryIds.values().iterator().next();

        JSONObject body = new JSONObject()
                .put("description", newDescription.replace("\"", ""));

        // Use POST for partial update (per API documentation)
        response = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories/" + existingId);
    }

    @Then("the category’s title remains unchanged")
    public void categoryTitleRemainsUnchanged() {
        String id = categoryIds.values().iterator().next();
        String originalTitle = originalTitles.get(id);

        Response getResponse = RestAssured.get(BASE_URL + "/categories/" + id);

        JSONObject body = new JSONObject(getResponse.asString());
        JSONObject category = body.getJSONArray("categories").getJSONObject(0);

        assertEquals(originalTitle, category.getString("title"),
                "Expected title to remain unchanged after partial update");
    }

    /* -------------------- Error Flow -------------------- */

    @When("the user updates category with id {int} to title {string}")
    public void userUpdatesNonexistentCategory(int invalidId, String newTitle) {
        JSONObject body = new JSONObject()
                .put("title", newTitle.replace("\"", ""));

        response = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .put(BASE_URL + "/categories/" + invalidId);
    }

    @Then("the update server responds with status {int}")
    public void updateServerRespondsWithStatus(int expectedStatus) {
        assertNotNull(response, "Response is null – the request did not execute properly");
        assertEquals(expectedStatus, response.statusCode(), "Unexpected HTTP status during update");
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

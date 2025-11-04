package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class Story12SearchCategorySteps {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private JSONArray categoriesArray;
    private final Map<String, String> categoryIds = new HashMap<>(); // title → id

    /* ---------------- Background ---------------- */

    @Given("the server is running for search category")
    public void theServerIsRunning() {
        RestAssured.get(BASE_URL + "/categories").then().statusCode(200);
    }

    @Given("categories with the following details exist for search category")
    public void categoriesExist(DataTable dataTable) {
        // Clean up any pre-existing categories
        Response getResponse = RestAssured.get(BASE_URL + "/categories");
        if (getResponse.statusCode() == 200 && getResponse.jsonPath().getList("categories") != null) {
            var existing = getResponse.jsonPath().getList("categories.id");
            for (Object id : existing) {
                RestAssured.delete(BASE_URL + "/categories/" + id);
            }
        }

        // Create new categories and record their IDs
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

            assertEquals(201, postResponse.statusCode(), "Failed to create category: " + title);
            String id = postResponse.jsonPath().getString("id");
            categoryIds.put(title, id);
        });

        System.out.println("✅ Created categories: " + categoryIds);
    }

    /* ---------------- Normal Flow ---------------- */

    @When("the user retrieves all categories")
    public void userRetrievesAllCategories() {
        response = RestAssured.get(BASE_URL + "/categories");
    }

    @Then("the server responds with status {int}")
    public void serverRespondsWithStatus(int statusCode) {
        assertEquals(statusCode, response.statusCode(), "Unexpected HTTP status code");
    }

    @Then("the response contains all existing categories")
    public void responseContainsAllExistingCategories() {
        JSONObject body = new JSONObject(response.asString());
        assertTrue(body.has("categories"), "Response does not contain 'categories'");
        categoriesArray = body.getJSONArray("categories");
        assertTrue(categoriesArray.length() > 0, "No categories found in response");
    }

    @Then("each category includes fields {string}, {string}, and {string}")
    public void categoryIncludesFields(String idField, String titleField, String descField) {
        for (int i = 0; i < categoriesArray.length(); i++) {
            JSONObject cat = categoriesArray.getJSONObject(i);
            assertTrue(cat.has(idField), "Missing field: " + idField);
            assertTrue(cat.has(titleField), "Missing field: " + titleField);
            assertTrue(cat.has(descField), "Missing field: " + descField);
        }
    }

    /* ---------------- Retrieve by Stored ID ---------------- */

    @When("the user retrieves the category with stored id for {string}")
    public void userRetrievesCategoryByStoredId(String title) {
        String id = categoryIds.get(title.replace("\"", ""));
        assertNotNull(id, "No stored ID found for category: " + title);
        response = RestAssured.get(BASE_URL + "/categories/" + id);
    }

    @Then("the response includes a category with title {string} and description {string}")
    public void responseIncludesCategoryWithTitleAndDescription(String title, String description) {
        JSONObject body = new JSONObject(response.asString());

        assertTrue(body.has("categories"), "Expected 'categories' array in response");

        JSONArray categories = body.getJSONArray("categories");
        assertTrue(categories.length() > 0, "Expected at least one category in response");

        JSONObject categoryObj = categories.getJSONObject(0);
        assertEquals(title.replace("\"", ""), categoryObj.getString("title"));
        assertEquals(description.replace("\"", ""), categoryObj.getString("description"));
    }

    /* ---------------- Error Flow ---------------- */

    @When("the user retrieves a category that does not exist")
    public void userRetrievesNonexistentCategory() {
        response = RestAssured.get(BASE_URL + "/categories/99999");
    }

    @Then("the user is notified that no results were found")
    public void userIsNotifiedNoResultsFound() {
        assertEquals(404, response.statusCode());
        System.out.println("✅ No matching categories found as expected.");
    }

    /* ---------------- Cleanup ---------------- */

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

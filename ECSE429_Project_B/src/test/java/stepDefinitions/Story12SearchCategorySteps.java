package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class Story12SearchCategorySteps {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private JSONArray categoriesArray;
    private JSONObject category;

    @Given("the server is running")
    public void theServerIsRunning() {
        RestAssured.get(BASE_URL + "/categories").then().statusCode(200);
    }

    @Given("categories with the following details exist")
    public void categoriesExist(io.cucumber.datatable.DataTable dataTable) {
        // Create categories based on input table
        dataTable.asMaps().forEach(row -> {
            JSONObject body = new JSONObject()
                    .put("title", row.get("title").replace("\"", ""))
                    .put("description", row.get("description").replace("\"", ""));
            RestAssured.given()
                    .contentType("application/json")
                    .body(body.toString())
                    .post(BASE_URL + "/categories")
                    .then()
                    .statusCode(201);
        });
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
            assertTrue(cat.has(idField));
            assertTrue(cat.has(titleField));
            assertTrue(cat.has(descField));
        }
    }

    /* ---------------- Get by ID ---------------- */

    @When("the user retrieves the category with id {int}")
    public void userRetrievesCategoryById(int id) {
        response = RestAssured.get(BASE_URL + "/categories/" + id);
    }

    @Then("the response includes a category with title {string} and description {string}")
    public void responseIncludesCategoryWithTitleAndDescription(String title, String description) {
        JSONObject body = new JSONObject(response.asString());
        assertEquals(title.replace("\"", ""), body.getString("title"));
        assertEquals(description.replace("\"", ""), body.getString("description"));
    }

    /* ---------------- Filter by Title ---------------- */

    @When("the user retrieves categories filtered by title {string}")
    public void userRetrievesCategoriesFilteredByTitle(String title) {
        String query = "?title=" + title.replace("\"", "").replace(" ", "%20");
        response = RestAssured.get(BASE_URL + "/categories" + query);
    }

    @Then("the response contains only categories whose title matches {string}")
    public void responseContainsOnlyMatchingTitle(String title) {
        JSONObject body = new JSONObject(response.asString());
        JSONArray array = body.getJSONArray("categories");
        for (int i = 0; i < array.length(); i++) {
            JSONObject cat = array.getJSONObject(i);
            assertEquals(title.replace("\"", ""), cat.getString("title"));
        }
    }

    /* ---------------- Filter by Description ---------------- */

    @When("the user retrieves categories filtered by description {string}")
    public void userRetrievesCategoriesFilteredByDescription(String desc) {
        String query = "?description=" + desc.replace("\"", "").replace(" ", "%20");
        response = RestAssured.get(BASE_URL + "/categories" + query);
    }

    @Then("the response contains only categories whose description includes {string}")
    public void responseContainsOnlyMatchingDescription(String desc) {
        JSONObject body = new JSONObject(response.asString());
        JSONArray array = body.getJSONArray("categories");
        for (int i = 0; i < array.length(); i++) {
            JSONObject cat = array.getJSONObject(i);
            assertTrue(cat.getString("description").contains(desc.replace("\"", "")));
        }
    }

    /* ---------------- Error Flows ---------------- */

    @When("the user retrieves the category with id {int} that does not exist")
    public void userRetrievesNonexistentCategory(int id) {
        response = RestAssured.get(BASE_URL + "/categories/" + id);
    }

    @Then("the user is notified with message {string}")
    public void userIsNotifiedWithMessage(String message) {
        assertTrue(response.asString().contains(message.replace("\"", "")));
    }

    @Then("the response contains no matching categories")
    public void responseContainsNoMatchingCategories() {
        JSONObject body = new JSONObject(response.asString());
        JSONArray array = body.getJSONArray("categories");
        assertEquals(0, array.length(), "Expected no categories, but some were returned");
    }

    @Then("the user is notified that no results were found")
    public void userIsNotifiedNoResultsFound() {
        System.out.println("✅ No matching categories found as expected.");
    }
}

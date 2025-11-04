package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Story11CreateCategorySteps {

    private final String BASE_URL = "http://localhost:4567";
    private Response response;
    private String createdTitle;
    private static final List<String> createdCategoryIds = new ArrayList<>();

    // ===== Background =====
    @Given("the server is running for category creation")
    public void theServerIsRunning() {
        RestAssured
                .get(BASE_URL + "/categories")
                .then()
                .statusCode(200);
    }

    @Given("the system has no existing categories")
    public void theSystemHasNoExistingCategories() {
        Response getResponse = RestAssured.get(BASE_URL + "/categories");
        if (getResponse.statusCode() == 200 && getResponse.jsonPath().getList("categories") != null) {
            var categories = getResponse.jsonPath().getList("categories.id");
            for (Object id : categories) {
                RestAssured.delete(BASE_URL + "/categories/" + id);
            }
        }
    }

    // ===== Normal & Alternate Flows =====
    @When("the user creates a category with title {string} and description {string}")
    public void userCreatesCategoryWithTitleAndDescription(String title, String description) {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description);

        response = RestAssured
                .given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories");

        // Store created ID for cleanup if creation succeeds
        if (response.statusCode() == 201) {
            String id = response.jsonPath().getString("id");
            if (id != null) createdCategoryIds.add(id);
        }

        createdTitle = title;
    }

    @When("the user creates a category with title {string} and no description")
    public void userCreatesCategoryWithTitleNoDescription(String title) {
        JSONObject body = new JSONObject();
        body.put("title", title);

        response = RestAssured
                .given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/categories");

        if (response.statusCode() == 201) {
            String id = response.jsonPath().getString("id");
            if (id != null) createdCategoryIds.add(id);
        }

        createdTitle = title;
    }

    @Then("the category with title {string} should be successfully created")
    public void categoryShouldBeSuccessfullyCreated(String title) {
        assertEquals(201, response.statusCode(), "Expected 201 Created response");
        String responseTitle = response.jsonPath().getString("title");
        assertEquals(title, responseTitle, "Category title mismatch");
    }

    @Then("the user is notified of the successful creation operation")
    public void userIsNotifiedOfSuccess() {
        assertTrue(
                response.asString().contains("id") || response.asString().contains("title"),
                "Expected response body to contain success data"
        );
    }

    // ===== Error Flow =====
    @Then("the API should respond with an error message {string}")
    public void apiShouldRespondWithErrorMessage(String message) {
        assertTrue(
                response.statusCode() == 400 || response.statusCode() == 500,
                "Expected 400 or 500 error code but got " + response.statusCode()
        );
        assertTrue(
                response.asString().toLowerCase().contains("error") ||
                        response.asString().contains(message),
                "Expected response to contain an error message"
        );
    }

    // ===== Cleanup After Each Scenario =====
    @After
    public void cleanupCreatedCategories() {
        if (!createdCategoryIds.isEmpty()) {
            for (String id : createdCategoryIds) {
                try {
                    RestAssured.delete(BASE_URL + "/categories/" + id);
                } catch (Exception e) {
                    System.err.println("Failed to delete category ID " + id + ": " + e.getMessage());
                }
            }
            createdCategoryIds.clear();
        }
    }
}

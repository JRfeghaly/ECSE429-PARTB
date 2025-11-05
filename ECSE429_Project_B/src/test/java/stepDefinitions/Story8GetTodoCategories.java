package stepDefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class Story8GetTodoCategories {

    private static final String BASE_URL = "http://localhost:4567";
    private Response response;
    private String todoId;
    private final List<String> createdCategoryIds = new ArrayList<>();

    /* -------------------- Background -------------------- */
    @Given("the server is running for Story8")
    public void theServerIsRunning() {
        Response res = RestAssured.get(BASE_URL + "/todos");
        assertEquals(200, res.statusCode(), "Server is not running on localhost:4567");
    }

    /* -------------------- Normal Flow -------------------- */
    @Given("a todo exists with id {int}")
    public void aTodoExistsWithId(int id) {
        JSONObject body = new JSONObject();
        body.put("title", "Todo " + id);

        Response postResponse = RestAssured.given()
                .contentType("application/json")
                .body(body.toString())
                .post(BASE_URL + "/todos");

        assertTrue(postResponse.statusCode() == 201 || postResponse.statusCode() == 200,
                "Failed to create todo with id " + id);

        todoId = postResponse.jsonPath().getString("id");
        System.out.println("Created todo with id: " + todoId);
    }

    @Given("the todo with id {int} has the following categories")
    public void theTodoWithIdHasTheFollowingCategories(int id, DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {
            // Create category resource first (canonical flow used elsewhere in the suite)
            String title = row.get("name").replace("\"", "");
            JSONObject category = new JSONObject();
            category.put("title", title);
            category.put("description", "");

            Response postCategory = RestAssured.given()
                    .contentType("application/json")
                    .body(category.toString())
                    .post(BASE_URL + "/categories");

            if (!(postCategory.statusCode() == 201 || postCategory.statusCode() == 200)) {
                System.out.println("Create category response: " + postCategory.asString());
            }
            assertTrue(postCategory.statusCode() == 201 || postCategory.statusCode() == 200,
                    "Failed to create category " + row.get("name") + "; response: " + postCategory.asString());

            String categoryId = postCategory.jsonPath().getString("id");
            createdCategoryIds.add(categoryId);

            // Link the created category to the todo
            Response link = RestAssured.given()
                    .contentType("application/json")
                    .body(Map.of("id", categoryId))
                    .post(BASE_URL + "/todos/" + todoId + "/categories");

            // Linking may return 201 (created) or 409 (already linked)
            if (!(link.statusCode() == 201 || link.statusCode() == 409)) {
                System.out.println("Link category->todo response: " + link.asString());
            }
            assertTrue(link.statusCode() == 201 || link.statusCode() == 409,
                    "Failed to link category " + title + " to todo " + todoId + "; response: " + link.asString());
        }
    }

    @After
    public void cleanupCreatedCategories() {
        if (createdCategoryIds != null && !createdCategoryIds.isEmpty()) {
            for (String id : createdCategoryIds) {
                try {
                    RestAssured.delete(BASE_URL + "/categories/" + id);
                    System.out.println("Deleted test category ID: " + id);
                } catch (Exception e) {
                    System.err.println("Failed to delete category ID " + id + ": " + e.getMessage());
                }
            }
            createdCategoryIds.clear();
        }
    }

    @When("the user requests categories for todo with id {int}")
    public void theUserRequestsCategoriesForTodoWithId(int id) {
        // Use server-created todoId when available (the feature uses logical ids like 1)
        String actualId = (todoId != null && !todoId.isEmpty()) ? todoId : Integer.toString(id);
        response = RestAssured.get(BASE_URL + "/todos/" + actualId + "/categories");
        System.out.println("Response for /todos/" + actualId + "/categories: " + response.asString());
    }

    @Then("the server should respond with status code {int} for Story8")
    public void theServerShouldRespondWithStatusCodeForStory8(int expectedStatus) {
        int actualStatus = response.statusCode();
        // If the server returned 200 when an error is expected, report this as a BUG (server returned success for a missing resource)
        if (actualStatus == 200 && expectedStatus != 200) {
            fail("BUG: Expected status " + expectedStatus + " but server returned 200. Response body: " + response.asString());
        }

        assertEquals(expectedStatus, actualStatus,
                "Expected status " + expectedStatus + " but got " + actualStatus);
    }

    @Then("the user should receive the following categories")
    public void theUserShouldReceiveTheFollowingCategories(DataTable dataTable) {
        String body = response.asString();

        JSONArray categoriesArray;
        try {
            if (body.trim().startsWith("{")) {
                JSONObject obj = new JSONObject(body);
                categoriesArray = obj.getJSONArray("categories");
            } else {
                categoriesArray = new JSONArray(body);
            }
        } catch (Exception e) {
            fail("Response did not contain valid JSON array of categories: " + body);
            return;
        }

        List<Map<String, String>> expected = dataTable.asMaps(String.class, String.class);
        assertEquals(expected.size(), categoriesArray.length(),
                "Mismatch in number of categories returned");

        // Build list of actual category names (order-independent)
        List<String> actualNames = new ArrayList<>();
        for (int i = 0; i < categoriesArray.length(); i++) {
            Object item = categoriesArray.get(i);
            String actualName = null;
            if (item instanceof JSONObject) {
                JSONObject actualObj = (JSONObject) item;
                if (actualObj.has("name")) actualName = actualObj.getString("name");
                else if (actualObj.has("title")) actualName = actualObj.getString("title");
                else if (actualObj.has("category")) actualName = actualObj.getString("category");
            } else if (item instanceof String) {
                actualName = (String) item;
            } else {
                actualName = item != null ? item.toString() : null;
            }
            if (actualName != null) actualNames.add(actualName);
        }

        List<String> expectedNames = new ArrayList<>();
        for (Map<String, String> row : expected) {
            expectedNames.add(row.get("name").replaceAll("^\"|\"$", ""));
        }

        // Verify counts match
        assertEquals(expectedNames.size(), actualNames.size(), "Mismatch in number of categories returned. Response: " + response.asString());

        // Verify all expected names are present regardless of order
        for (String en : expectedNames) {
            assertTrue(actualNames.contains(en), "Expected category not found: " + en + ". Actual: " + actualNames + ". Response: " + response.asString());
        }
    }

    /* -------------------- Error Flow -------------------- */
    @Given("a todo with id {int} does not exist")
    public void aTodoWithIdDoesNotExist(int id) {
        Response res = RestAssured.get(BASE_URL + "/todos/" + id);
        if (res.statusCode() == 200) {
            RestAssured.delete(BASE_URL + "/todos/" + id);
        }
        Response check = RestAssured.get(BASE_URL + "/todos/" + id);
        assertEquals(404, check.statusCode(), "Todo with id " + id + " still exists!");
    }

    @Then("the user should receive a warning that categories are invalid")
    public void theUserShouldReceiveAWarningThatCategoriesAreInvalid() {
        String body = response.asString();
        int status = response.statusCode();

        // If the server returned 200 when it should have returned 404 for a missing todo -> BUG
        if (status == 200) {
            fail("BUG: Server returned 200 when 404 expected for non-existent todo. Response: " + body);
        }
    }
}

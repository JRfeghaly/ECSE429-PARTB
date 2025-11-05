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

public class Story8GetTodoCategories {

    private static final String BASE_URL = "http://localhost:4567";
    private Response response;
    private String todoId;

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
            JSONObject category = new JSONObject();
            category.put("name", row.get("name"));

            Response postCategory = RestAssured.given()
                    .contentType("application/json")
                    .body(category.toString())
                    .post(BASE_URL + "/todos/" + todoId + "/categories");

            assertTrue(postCategory.statusCode() == 201 || postCategory.statusCode() == 200,
                    "Failed to create category " + row.get("name"));
        }
    }

    @When("the user requests categories for todo with id {int}")
    public void theUserRequestsCategoriesForTodoWithId(int id) {
        response = RestAssured.get(BASE_URL + "/todos/" + id + "/categories");
        System.out.println("Response for /todos/" + id + "/categories: " + response.asString());
    }

    @Then("the server should respond with status code {int} for Story8")
    public void theServerShouldRespondWithStatusCodeForStory8(int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(),
                "Expected status " + expectedStatus + " but got " + response.statusCode());
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

        for (int i = 0; i < expected.size(); i++) {
            String expectedName = expected.get(i).get("name");
            String actualName = categoriesArray.getJSONObject(i).getString("name");
            assertEquals(expectedName, actualName, "Category name mismatch at index " + i);
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
        boolean bugDetected = body.contains("categories") && response.statusCode() == 200;

        // This assertion is designed to FAIL (to expose the bug)
        assertFalse(bugDetected,
                "BUG DETECTED: Server returned categories for a non-existent todo! Response: " + body);
    }
}

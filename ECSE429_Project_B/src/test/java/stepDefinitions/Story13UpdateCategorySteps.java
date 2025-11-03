package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class Story13UpdateCategorySteps {
    private Response response;
    private final String BASE_URL = "http://localhost:4567";

    @When("I update category {string} with new title {string}")
    public void updateCategory(String id, String newTitle) {
        JSONObject body = new JSONObject().put("title", newTitle);
        response = RestAssured.given().contentType("application/json")
                .body(body.toString()).put(BASE_URL + "/categories/" + id);
    }

    @Then("the category title should be updated")
    public void categoryUpdated() {
        assertEquals(200, response.statusCode());
        assertTrue(response.asString().contains("title"));
    }

    @When("I update non existent category {string}")
    public void updateInvalid(String id) {
        JSONObject body = new JSONObject().put("title", "NoSuchCat");
        response = RestAssured.given().contentType("application/json")
                .body(body.toString()).put(BASE_URL + "/categories/" + id);
    }

    @Then("the API should return not found")
    public void notFound() {
        assertEquals(404, response.statusCode());
    }
}

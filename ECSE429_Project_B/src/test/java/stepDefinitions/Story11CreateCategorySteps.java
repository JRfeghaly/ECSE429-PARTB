package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class Story11CreateCategorySteps {
    private Response response;
    private final String BASE_URL = "http://localhost:4567";

    @Given("the API service is running for category creation")
    public void apiRunning() {
        RestAssured.get(BASE_URL + "/categories").then().statusCode(200);
    }

    @When("I create a category with title {string} and description {string}")
    public void createCategory(String title, String desc) {
        JSONObject body = new JSONObject().put("title", title).put("description", desc);
        response = RestAssured.given().contentType("application/json")
                .body(body.toString()).post(BASE_URL + "/categories");
    }

    @Then("the category should be created successfully")
    public void categoryCreated() {
        assertEquals(201, response.statusCode());
        assertTrue(response.asString().contains("id"));
    }

    @When("I create a category with empty title")
    public void createCategoryInvalid() {
        JSONObject body = new JSONObject().put("title", "").put("description", "Invalid");
        response = RestAssured.given().contentType("application/json")
                .body(body.toString()).post(BASE_URL + "/categories");
    }

    @Then("the API should return bad request")
    public void badRequest() {
        assertEquals(400, response.statusCode());
    }
}

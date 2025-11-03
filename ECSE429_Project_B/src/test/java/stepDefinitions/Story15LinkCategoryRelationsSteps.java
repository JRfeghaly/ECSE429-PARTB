package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

public class Story15LinkCategoryRelationsSteps {
    private Response response;
    private final String BASE_URL = "http://localhost:4567";

    @When("I link todo {string} to category {string}")
    public void linkTodo(String todoId, String catId) {
        JSONObject body = new JSONObject().put("id", todoId);
        response = RestAssured.given().contentType("application/json")
                .body(body.toString()).post(BASE_URL + "/categories/" + catId + "/todos");
    }

    @Then("the todo should be linked successfully")
    public void todoLinked() {
        assertTrue(response.statusCode() == 201 || response.statusCode() == 200);
    }
}

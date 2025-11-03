package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static org.junit.jupiter.api.Assertions.*;

public class Story14DeleteCategorySteps {
    private Response response;
    private final String BASE_URL = "http://localhost:4567";

    @When("I delete category {string}")
    public void deleteCategory(String id) {
        response = RestAssured.delete(BASE_URL + "/categories/" + id);
    }

    @Then("the category should be deleted successfully")
    public void categoryDeleted() {
        assertTrue(response.statusCode() == 200 || response.statusCode() == 404);
    }
}

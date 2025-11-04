package stepDefinitions;

import io.cucumber.java.en.Given;
import io.restassured.RestAssured;

public class CommonSteps {
    private final String BASE_URL = "http://localhost:4567";

    @Given("the server is running")
    public void theServerIsRunning() {
        RestAssured.get(BASE_URL + "/projects").then().statusCode(200);
    }
}
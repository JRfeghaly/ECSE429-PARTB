package stepDefinitions;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class Story2AddANewToDo {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user creates a new todo with title {string} and description {string}")
    public void the_user_creates_todo_with_title_and_description(String title, String description) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        CommonSteps.lastResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @When("the user creates a todo with empty title and description {string}")
    public void the_user_creates_todo_with_empty_title(String description) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", "");
        body.put("description", description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        CommonSteps.lastResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response contains a generated id")
    public void the_response_contains_generated_id() {
        JSONObject json = new JSONObject(CommonSteps.lastResponse.body());
        assertTrue(json.has("id"));
    }

    @Then("the todo with title {string} exists")
    public void the_todo_with_title_exists(String title) throws Exception {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertTrue(getResponse.body().contains(title));
    }

    @Then("the response contains error message with keyword {string}")
    public void the_response_contains_error_with_keyword(String keyword) {
        assertTrue(CommonSteps.lastResponse.body().toLowerCase().contains(keyword.toLowerCase()));
    }
}
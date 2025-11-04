package stepDefinitions;

import io.cucumber.java.en.*;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.json.JSONObject;
import org.json.JSONArray;

public class Story1ViewAllToDos {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @Given("at least one todo exists")
    public void at_least_one_todo_exists() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        JSONArray arr = new JSONObject(res.body()).optJSONArray("todos");
        assertNotNull(arr);
        assertTrue(arr.length() > 0);
    }

    @When("the user retrieves all todos")
    public void the_user_retrieves_all_todos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .build();
        CommonSteps.lastResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @When("the user retrieves the todo with id {int}")
    public void the_user_retrieves_todo_by_id(int id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .build();
        CommonSteps.lastResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response contains a list of todos")
    public void the_response_contains_list_of_todos() {
        JSONObject json = new JSONObject(CommonSteps.lastResponse.body());
        assertTrue(json.has("todos"));
    }

    @Then("the response contains the todo with id {int}")
    public void the_response_contains_todo_with_id(int id) {
        assertTrue(CommonSteps.lastResponse.body().contains("\"id\":" + id) || CommonSteps.lastResponse.body().contains("\"id\":\"" + id + "\""));
    }
}

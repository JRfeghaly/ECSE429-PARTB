package stepDefinitions;

import io.cucumber.java.en.*;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.json.JSONObject;
import org.json.JSONArray;

public class Story1ViewAllToDos {

    private HttpResponse<String> response;
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @Given("the server is running")
    public void the_server_is_running() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(res.statusCode() == 200 || res.statusCode() == 404);
    }

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
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @When("the user retrieves the todo with id {int}")
    public void the_user_retrieves_todo_by_id(int id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response contains a list of todos")
    public void the_response_contains_list_of_todos() {
        JSONObject json = new JSONObject(response.body());
        assertTrue(json.has("todos"));
    }

    @Then("the response contains the todo with id {int}")
    public void the_response_contains_todo_with_id(int id) {
        assertTrue(response.body().contains("\"id\":" + id) || response.body().contains("\"id\":\"" + id + "\""));
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int code) {
        assertEquals(code, response.statusCode());
    }
}

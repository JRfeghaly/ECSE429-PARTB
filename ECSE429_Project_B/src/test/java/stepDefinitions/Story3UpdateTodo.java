package stepDefinitions;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class Story3UpdateTodo {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user updates todo with id {int} to have title {string} and description {string}")
    public void the_user_updates_todo_with_title_and_description(int id, String title, String description) throws Exception {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the todo with id {int} has title {string}")
    public void the_todo_with_id_has_title(int id, String title) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .build();
        HttpResponse<String> getRes = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(getRes.body().contains(title));
    }
}

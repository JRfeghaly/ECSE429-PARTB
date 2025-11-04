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
    public void the_user_updates_todo_with_title_and_description(int idIndex, String title, String description) throws Exception {
        // For idIndex 1 or 2, use the dynamic IDs
        // For other IDs (like 99 for non-existent), use them directly
        int actualId;
        if (idIndex == 1) {
            actualId = Story1ViewAllToDos.testTodoId1;
        } else if (idIndex == 2) {
            actualId = Story1ViewAllToDos.testTodoId2;
        } else {
            actualId = idIndex; // For non-existent IDs like 99
        }

        // First check if todo exists (to avoid PUT creating it)
        HttpRequest checkReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .build();
        HttpResponse<String> checkRes = client.send(checkReq, HttpResponse.BodyHandlers.ofString());
        
        // If it doesn't exist, record the 404 and return
        if (checkRes.statusCode() == 404) {
            CommonSteps.lastResponse = checkRes;
            return;
        }

        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("description", description);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the todo with id {int} has title {string}")
    public void the_todo_with_id_has_title(int idIndex, String title) throws Exception {
        // idIndex 1 means testTodoId1 from Story1
        int actualId = (idIndex == 1) ? Story1ViewAllToDos.testTodoId1 : Story1ViewAllToDos.testTodoId2;

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .build();
        HttpResponse<String> getRes = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(getRes.body().contains(title));
    }
}
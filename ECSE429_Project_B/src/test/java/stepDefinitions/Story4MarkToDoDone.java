package stepDefinitions;

import io.cucumber.java.en.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class Story4MarkToDoDone {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user marks todo with id {int} as done")
    public void the_user_marks_todo_as_done(int id) throws Exception {
        JSONObject body = new JSONObject();
        body.put("doneStatus", true);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

package stepDefinitions;

import io.cucumber.java.en.*;
import java.net.http.*;
import java.net.URI;

public class Story5DeleteToDo {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user deletes todo with id {int}")
    public void the_user_deletes_todo(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .DELETE()
                .build();
        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

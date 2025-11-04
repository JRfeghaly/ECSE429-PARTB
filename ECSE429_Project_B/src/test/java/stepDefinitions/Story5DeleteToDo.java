package stepDefinitions;

import io.cucumber.java.en.*;
import java.net.http.*;
import java.net.URI;

public class Story5DeleteToDo {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user deletes todo with id {int}")
    public void the_user_deletes_todo(int idIndex) throws Exception {
        // For idIndex 1 and 2, use the dynamic IDs from Story1
        // For other IDs (like 500 for non-existent), use them directly
        int actualId;
        if (idIndex == 1) {
            actualId = Story1ViewAllToDos.testTodoId1;
        } else if (idIndex == 2) {
            actualId = Story1ViewAllToDos.testTodoId2;
        } else {
            actualId = idIndex; // For non-existent IDs like 500
        }
        
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .DELETE()
                .build();
        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

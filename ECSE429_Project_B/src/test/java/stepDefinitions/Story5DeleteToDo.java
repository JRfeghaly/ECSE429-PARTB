package stepDefinitions;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.URI;

public class Story5DeleteToDo {
    private HttpResponse<String> response;
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user deletes todo with id {int}")
    public void the_user_deletes_todo(int id) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + id))
                .DELETE()
                .build();
        response = client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int code) {
        assertEquals(code, response.statusCode());
    }
}

package stepDefinitions;

import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.URI;

public class CommonSteps {
    
    private static final String BASE_URL = "http://localhost:4567";
    private static final HttpClient client = HttpClient.newHttpClient();
    
    // Shared response holder for all step definition classes
    public static HttpResponse<String> lastResponse;

    @Given("the server is running")
    public void the_server_is_running() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertTrue(res.statusCode() == 200 || res.statusCode() == 404, "Server is not running");
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int code) {
        assertNotNull(lastResponse, "No response has been recorded. Make sure a request was sent first.");
        assertEquals(code, lastResponse.statusCode(), "Expected status " + code + " but got " + lastResponse.statusCode());
    }
}

package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

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

    // ===== Centralized Cleanup for Test Todos (runs after EVERY scenario) =====
    @After
    public void restoreTestTodos() {
        // Restore testTodoId1 if it was modified or deleted
        if (Story1ViewAllToDos.testTodoId1 != -1) {
            try {
                HttpRequest checkReq = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/todos/" + Story1ViewAllToDos.testTodoId1))
                        .build();
                HttpResponse<String> checkRes = client.send(checkReq, HttpResponse.BodyHandlers.ofString());
                
                if (checkRes.statusCode() == 404) {
                    // Todo was deleted, recreate it
                    String body = "{\"title\":\"Test Todo 1\",\"doneStatus\":false,\"description\":\"Test description 1\"}";
                    HttpRequest createReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/todos"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();
                    HttpResponse<String> createRes = client.send(createReq, HttpResponse.BodyHandlers.ofString());
                    JSONObject todo = new JSONObject(createRes.body());
                    Story1ViewAllToDos.testTodoId1 = Integer.parseInt(todo.getString("id"));
                } else {
                    // Todo exists, reset to original state
                    JSONObject resetBody = new JSONObject();
                    resetBody.put("title", "Test Todo 1");
                    resetBody.put("description", "Test description 1");
                    resetBody.put("doneStatus", false);
                    
                    HttpRequest resetReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/todos/" + Story1ViewAllToDos.testTodoId1))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(resetBody.toString()))
                            .build();
                    client.send(resetReq, HttpResponse.BodyHandlers.ofString());
                }
            } catch (Exception e) {
                System.err.println("Failed to restore testTodoId1: " + e.getMessage());
            }
        }

        // Restore testTodoId2 if it was modified or deleted
        if (Story1ViewAllToDos.testTodoId2 != -1) {
            try {
                HttpRequest checkReq = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/todos/" + Story1ViewAllToDos.testTodoId2))
                        .build();
                HttpResponse<String> checkRes = client.send(checkReq, HttpResponse.BodyHandlers.ofString());
                
                if (checkRes.statusCode() == 404) {
                    // Todo was deleted, recreate it
                    String body = "{\"title\":\"Test Todo 2\",\"doneStatus\":false,\"description\":\"Test description 2\"}";
                    HttpRequest createReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/todos"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build();
                    HttpResponse<String> createRes = client.send(createReq, HttpResponse.BodyHandlers.ofString());
                    JSONObject todo = new JSONObject(createRes.body());
                    Story1ViewAllToDos.testTodoId2 = Integer.parseInt(todo.getString("id"));
                } else {
                    // Todo exists, reset to original state
                    JSONObject resetBody = new JSONObject();
                    resetBody.put("title", "Test Todo 2");
                    resetBody.put("description", "Test description 2");
                    resetBody.put("doneStatus", false);
                    
                    HttpRequest resetReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/todos/" + Story1ViewAllToDos.testTodoId2))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString(resetBody.toString()))
                            .build();
                    client.send(resetReq, HttpResponse.BodyHandlers.ofString());
                }
            } catch (Exception e) {
                System.err.println("Failed to restore testTodoId2: " + e.getMessage());
            }
        }
    }
}
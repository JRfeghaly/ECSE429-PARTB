package stepDefinitions;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import java.net.http.*;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;

public class Story1ViewAllToDos {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";
    public static int testTodoId1 = -1;
    public static int testTodoId2 = -1;
    private static final List<Integer> createdTodoIds = new ArrayList<>();

    @Given("at least one todo exists")
    public void at_least_one_todo_exists() throws Exception {
        // Get current todos
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos"))
                .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        JSONObject responseBody = new JSONObject(res.body());
        JSONArray arr = responseBody.optJSONArray("todos");

        // If we have less than 2 todos, create them
        if (arr == null || arr.length() < 2) {
            // Create todo 1
            String body1 = "{\"title\":\"Test Todo 1\",\"doneStatus\":\"false\",\"description\":\"Test description 1\"}";
            HttpRequest createReq1 = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body1))
                    .build();
            HttpResponse<String> createRes1 = client.send(createReq1, HttpResponse.BodyHandlers.ofString());
            JSONObject todo1 = new JSONObject(createRes1.body());
            testTodoId1 = Integer.parseInt(todo1.getString("id"));
            createdTodoIds.add(testTodoId1);

            // Create todo 2
            String body2 = "{\"title\":\"Test Todo 2\",\"doneStatus\":\"false\",\"description\":\"Test description 2\"}";
            HttpRequest createReq2 = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/todos"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body2))
                    .build();
            HttpResponse<String> createRes2 = client.send(createReq2, HttpResponse.BodyHandlers.ofString());
            JSONObject todo2 = new JSONObject(createRes2.body());
            testTodoId2 = Integer.parseInt(todo2.getString("id"));
            createdTodoIds.add(testTodoId2);
        } else {
            // Use existing todos
            testTodoId1 = Integer.parseInt(arr.getJSONObject(0).getString("id"));
            if (arr.length() > 1) {
                testTodoId2 = Integer.parseInt(arr.getJSONObject(1).getString("id"));
            } else {
                testTodoId2 = testTodoId1;
            }
        }

        // Verify at least one todo exists now
        res = client.send(req, HttpResponse.BodyHandlers.ofString());
        arr = new JSONObject(res.body()).optJSONArray("todos");
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
    public void the_user_retrieves_the_todo_with_id(Integer idIndex) throws Exception {
        // For idIndex 1 or 2, use the dynamic IDs
        // For other IDs (like 99, 500 for non-existent), use them directly
        int actualId;
        if (idIndex == 1) {
            actualId = testTodoId1;
        } else if (idIndex == 2) {
            actualId = testTodoId2;
        } else {
            actualId = idIndex; // For non-existent IDs like 99, 500
        }

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .build();
        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response contains a list of todos")
    public void the_response_contains_list_of_todos() {
        JSONObject json = new JSONObject(CommonSteps.lastResponse.body());
        assertTrue(json.has("todos"));
    }

    @Then("the response contains the todo with id {int}")
    public void the_response_contains_todo_with_id(int idIndex) {
        // idIndex 1 means testTodoId1, idIndex 2 means testTodoId2
        int actualId = (idIndex == 1) ? testTodoId1 : testTodoId2;
        assertTrue(CommonSteps.lastResponse.body().contains("\"id\":" + actualId) || CommonSteps.lastResponse.body().contains("\"id\":\"" + actualId + "\""));
    }

    // ===== Cleanup After Each Scenario =====
    @After
    public void cleanupCreatedTodos() {
        // CommonSteps @After handles restoring testTodoId1 and testTodoId2
        // We only need to delete additional todos created during this scenario
        if (!createdTodoIds.isEmpty()) {
            for (Integer id : createdTodoIds) {
                try {
                    HttpRequest deleteReq = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/todos/" + id))
                            .DELETE()
                            .build();
                    client.send(deleteReq, HttpResponse.BodyHandlers.ofString());
                } catch (Exception e) {
                    System.err.println("Failed to delete todo ID " + id + ": " + e.getMessage());
                }
            }
            createdTodoIds.clear();
        }
    }
}
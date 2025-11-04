package stepDefinitions;

import io.cucumber.java.en.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class Story4MarkToDoDone {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE_URL = "http://localhost:4567";

    @When("the user marks todo with id {int} as done")
    public void the_user_marks_todo_as_done(int idIndex) throws Exception {
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
        
        // First get the existing todo to retrieve its current state
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .build();
        HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        
        // If todo doesn't exist, just try to mark it done anyway (will get 404)
        String body;
        if (getRes.statusCode() == 200) {
            JSONObject responseObj = new JSONObject(getRes.body());
            
            // Check if response has "todos" array or direct object
            JSONObject todo;
            if (responseObj.has("todos")) {
                todo = responseObj.getJSONArray("todos").getJSONObject(0);
            } else {
                todo = responseObj;
            }
            
            JSONObject updateBody = new JSONObject();
            
            // Include all fields from the existing todo
            if (todo.has("title")) {
                updateBody.put("title", todo.getString("title"));
            }
            if (todo.has("description")) {
                updateBody.put("description", todo.getString("description"));
            }
            updateBody.put("doneStatus", true);  // Send as boolean
            
            body = updateBody.toString();
        } else {
            body = "{\"doneStatus\":\"true\"}";
        }
        
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/todos/" + actualId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();
        CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
    }
}

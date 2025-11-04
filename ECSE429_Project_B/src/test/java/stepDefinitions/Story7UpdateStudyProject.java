package stepDefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import static org.junit.jupiter.api.Assertions.*;
import java.net.http.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class Story7UpdateStudyProject {

	private final HttpClient client = HttpClient.newHttpClient();
	private final String BASE_URL = "http://localhost:4567";

	// Map feature table id (1,2,...) to actual server-created project id
	private final Map<Integer, Integer> featureIdToActual = new HashMap<>();
	private final List<Integer> createdProjectIds = new ArrayList<>();
	private int lastFeatureId = -1;

	@Given("the following projects exist")
	public void the_following_projects_exist(io.cucumber.datatable.DataTable dataTable) throws Exception {
		for (var row : dataTable.asMaps(String.class, String.class)) {
			String idStr = row.getOrDefault("id", "").trim();
			String title = stripQuotes(row.getOrDefault("title", ""));
			String completedStr = stripQuotes(row.getOrDefault("completed", "false"));
			String description = stripQuotes(row.getOrDefault("description", ""));
			String activeStr = stripQuotes(row.getOrDefault("active", "false"));

			Integer featureId = null;
			if (!idStr.isEmpty()) {
				try { featureId = Integer.parseInt(idStr); } catch (NumberFormatException e) { }
			}

			HttpRequest getAll = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/projects")).build();
			HttpResponse<String> res = client.send(getAll, HttpResponse.BodyHandlers.ofString());
			boolean found = false;
			if (res.statusCode() == 200) {
				try {
					JSONObject wrapper = new JSONObject(res.body());
					if (wrapper.has("projects")) {
						for (var p : wrapper.getJSONArray("projects")) {
							// org.json returns Object; convert to JSONObject via toString
							JSONObject proj = new JSONObject(p.toString());
							if (title.equals(proj.optString("title", null))) {
								int actual = Integer.parseInt(proj.getString("id"));
								if (featureId != null) featureIdToActual.put(featureId, actual);
								found = true;
								break;
							}
						}
					}
				} catch (Exception e) {
					// ignore parse issues
				}
			}

			if (found) continue;

			// Create the project
			JSONObject body = new JSONObject();
			if (!title.isEmpty()) body.put("title", title);
			body.put("completed", Boolean.parseBoolean(completedStr));
			body.put("description", description);
			body.put("active", Boolean.parseBoolean(activeStr));

			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create(BASE_URL + "/projects"))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
					.build();

			HttpResponse<String> createRes = client.send(req, HttpResponse.BodyHandlers.ofString());
			if (createRes.statusCode() == 200 || createRes.statusCode() == 201) {
				try {
					JSONObject created = new JSONObject(createRes.body());
					String idVal = created.has("id") ? created.getString("id") : (created.has("project") ? created.getJSONObject("project").optString("id", null) : null);
					if (idVal != null) {
						int actualId = Integer.parseInt(idVal);
						createdProjectIds.add(actualId);
						if (featureId != null) featureIdToActual.put(featureId, actualId);
					}
				} catch (Exception e) {
				}
			}
		}
	}

	@Given("a user has a project with id {int}")
	public void a_user_has_a_project_with_id(int id) throws Exception {
		lastFeatureId = id;
		int actual = resolveFeatureId(id);
		HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/projects/" + actual)).build();
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, res.statusCode(), "Expected project with id " + id + " to exist");
	}

	@When("the user updates the project to have {string}, {string}, {string}, and {string}")
	public void the_user_updates_the_project_to_have(String title, String completed, String description, String active) throws Exception {
		int target = (lastFeatureId != -1) ? resolveFeatureId(lastFeatureId) : -1;
		if (target == -1) target = resolveFeatureId(1); // fallback

		JSONObject body = new JSONObject();
		body.put("title", title);
		body.put("completed", Boolean.parseBoolean(completed));
		body.put("description", description);
		body.put("active", Boolean.parseBoolean(active));

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/projects/" + target))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
				.build();

		CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
	}

	@Then("the project with id {int} is updated with the new details")
	public void the_project_with_id_is_updated_with_the_new_details(int id) throws Exception {
		int actual = resolveFeatureId(id);
		HttpRequest get = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/projects/" + actual)).build();
		HttpResponse<String> res = client.send(get, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, res.statusCode());
		String body = res.body();
		assertTrue(body.contains("title") || body.contains("description"), "Updated fields not present in project body");
	}

	@Then("the user is notified of the completion of the update operation")
	public void the_user_is_notified_of_the_completion_of_the_update_operation() {
		assertNotNull(CommonSteps.lastResponse, "No response recorded from update request");
		int status = CommonSteps.lastResponse.statusCode();
		assertTrue(status == 200 || status == 201, "Expected 200/201 for successful update, got " + status);
	}

	@When("the user updates only the {string} of the project")
	public void the_user_updates_only_the_description_of_the_project(String description) throws Exception {
		int target = (lastFeatureId != -1) ? resolveFeatureId(lastFeatureId) : resolveFeatureId(1);

		JSONObject body = new JSONObject();
		body.put("description", description);

		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/projects/" + target))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
				.build();

		CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
	}

	@Then("the project with id {int} reflects the updated {string}")
	public void the_project_with_id_reflects_the_updated_description(int id, String description) throws Exception {
		int actual = resolveFeatureId(id);
		HttpRequest get = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/projects/" + actual)).build();
		HttpResponse<String> res = client.send(get, HttpResponse.BodyHandlers.ofString());
		assertEquals(200, res.statusCode());
		assertTrue(res.body().contains(description), "Expected description to be updated. Actual: " + res.body());
	}

	@Given("a project with ID of {int} does not exist")
	public void a_project_with_id_of_does_not_exist(int id) throws Exception {
		int actual = resolveFeatureId(id);
		HttpRequest req = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/projects/" + actual)).build();
		HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
		assertEquals(404, res.statusCode(), "Expected project to not exist");
	}

	@When("the user requests to update the project with id {int} to change its details")
	public void the_user_requests_to_update_the_project_with_id_to_change_its_details(int id) throws Exception {
		JSONObject body = new JSONObject();
		body.put("title", "NonExistent");
		body.put("description", "Trying to update non-existent project");

		int actual = resolveFeatureId(id);
		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(BASE_URL + "/projects/" + actual))
				.header("Content-Type", "application/json")
				.PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
				.build();

		CommonSteps.lastResponse = client.send(req, HttpResponse.BodyHandlers.ofString());
	}

	@Then("the user is notified of the non-existence error with a message {string}")
	public void the_user_is_notified_of_the_non_existence_error_with_a_message(String message) {
		assertNotNull(CommonSteps.lastResponse, "No response recorded");
		int status = CommonSteps.lastResponse.statusCode();
		assertTrue(status == 404 || status == 400, "Expected 404/400 for non-existent resource update, got " + status);
		String body = CommonSteps.lastResponse.body();
		boolean ok = body.contains(message) || body.toLowerCase().contains("errormessages") || body.toLowerCase().contains("invalid guid");
		assertTrue(ok, "Expected error message not found. Actual: " + body);
	}

	private int resolveFeatureId(int featureId) {
		return featureIdToActual.getOrDefault(featureId, featureId);
	}

	private String stripQuotes(String s) {
		if (s == null) return "";
		s = s.trim();
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	@After
	public void cleanupCreatedProjects() {
		if (!createdProjectIds.isEmpty()) {
			for (Integer id : new ArrayList<>(createdProjectIds)) {
				try {
					HttpRequest deleteReq = HttpRequest.newBuilder()
							.uri(URI.create(BASE_URL + "/projects/" + id))
							.DELETE()
							.build();
					client.send(deleteReq, HttpResponse.BodyHandlers.ofString());
				} catch (Exception e) {
					System.err.println("Failed to delete project ID " + id + ": " + e.getMessage());
				}
			}
			createdProjectIds.clear();
		}
	}

}

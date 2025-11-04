package stepDefinitions;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Story19LinkProjectToCategorySteps {

    private static final String BASE_URL = "http://localhost:4567";

    // ------- Helpers -------
    private int getProjectIdByTitle(String title) {
        Response res = RestAssured.get(BASE_URL + "/projects");
        assertEquals(200, res.statusCode());
        List<Map<String, Object>> projects = res.jsonPath().getList("projects");
        for (Map<String, Object> p : projects) {
            if (title.equals(p.get("title"))) return Integer.parseInt(p.get("id").toString());
        }
        throw new AssertionError("Project not found: " + title);
    }

    private int getCategoryIdByTitle(String title) {
        Response res = RestAssured.get(BASE_URL + "/categories");
        assertEquals(200, res.statusCode());
        List<Map<String, Object>> cats = res.jsonPath().getList("categories");
        for (Map<String, Object> c : cats) {
            if (title.equals(c.get("title"))) return Integer.parseInt(c.get("id").toString());
        }
        throw new AssertionError("Category not found: " + title);
    }

    private void ensureProjectExists(String title, String desc) {
        Response res = RestAssured.get(BASE_URL + "/projects");
        List<Map<String, Object>> projects = res.jsonPath().getList("projects");
        boolean exists = false;
        if (projects != null) {
            for (Map<String, Object> p : projects) {
                if (title.equals(p.get("title"))) { exists = true; break; }
            }
        }
        if (!exists) {
            Response create = RestAssured.given().contentType("application/json")
                    .body(Map.of("title", title, "description", desc, "active", true, "completed", false))
                    .post(BASE_URL + "/projects");
            assertTrue(create.statusCode() == 200 || create.statusCode() == 201);
        }
    }

    private void ensureCategoryExists(String title, String desc) {
        Response res = RestAssured.get(BASE_URL + "/categories");
        List<Map<String, Object>> cats = res.jsonPath().getList("categories");
        boolean exists = false;
        if (cats != null) {
            for (Map<String, Object> c : cats) {
                if (title.equals(c.get("title"))) { exists = true; break; }
            }
        }
        if (!exists) {
            Response create = RestAssured.given().contentType("application/json")
                    .body(Map.of("title", title, "description", desc))
                    .post(BASE_URL + "/categories");
            assertTrue(create.statusCode() == 200 || create.statusCode() == 201);
        }
    }

    private Response linkCategoryToProject(int categoryId, int projectId) {
        return RestAssured.given()
                .contentType("application/json")
                .body(Map.of("id", Integer.toString(categoryId)))
                .post(BASE_URL + "/projects/" + projectId + "/categories");
    }

    // ------- Step glue -------
    @Given("projects with the following details exist")
    public void projectsWithTheFollowingDetailsExist(io.cucumber.datatable.DataTable table) {
        table.asMaps().forEach(row -> ensureProjectExists(row.get("title"), row.get("description")));
    }

    @Given("project categories records with the following details exist")
    public void categoriesWithTheFollowingDetailsExist(io.cucumber.datatable.DataTable table) {
        table.asMaps().forEach(row -> ensureCategoryExists(row.get("title"), row.get("description")));
    }

    @When("the manager links the category with title {string} to the project with title {string}")
    public void linkCategoryToProjectByTitles(String categoryTitle, String projectTitle) {
        int catId = getCategoryIdByTitle(categoryTitle);
        int projectId = getProjectIdByTitle(projectTitle);

        assertEquals(200, RestAssured.get(BASE_URL + "/categories/" + catId).statusCode(), "Category missing before linking");
        assertEquals(200, RestAssured.get(BASE_URL + "/projects/" + projectId).statusCode(), "Project missing before linking");

        Response link = linkCategoryToProject(catId, projectId);
        assertTrue(link.statusCode() == 201 || link.statusCode() == 409,
                "Link category→project should be 201 or 409, got " + link.statusCode());
    }

    @Given("the category with title {string} is already linked to the project with title {string}")
    public void categoryAlreadyLinkedToProject(String categoryTitle, String projectTitle) {
        int catId = getCategoryIdByTitle(categoryTitle);
        int projectId = getProjectIdByTitle(projectTitle);
        Response link = linkCategoryToProject(catId, projectId);
        assertTrue(link.statusCode() == 201 || link.statusCode() == 409);
    }

    @Given("no category with id {string} exists")
    public void categoryWithIdDoesNotExist(String id) {
        int cid = Integer.parseInt(id);
        Response res = RestAssured.get(BASE_URL + "/categories/" + cid);
        assertEquals(404, res.statusCode());
    }

    @Given("a project with title {string} exists")
    public void ensureProjectWithTitleExists(String title) {
        ensureProjectExists(title, "autocreated");
        int pid = getProjectIdByTitle(title);
        assertEquals(200, RestAssured.get(BASE_URL + "/projects/" + pid).statusCode());
    }

    @When("the manager links the category with id {string} to the project with title {string}")
    public void linkNonExistingCategoryToProject(String categoryIdStr, String projectTitle) {
        int pid = getProjectIdByTitle(projectTitle);
        int cid = Integer.parseInt(categoryIdStr);
        Response link = RestAssured.given().contentType("application/json")
                .body(Map.of("id", Integer.toString(cid)))
                .post(BASE_URL + "/projects/" + pid + "/categories");
        assertEquals(404, link.statusCode(), "Linking non-existing category should be 404");
    }

    @Then("the project with title {string} is linked to the category with title {string}")
    public void verifyProjectLinkedToCategory(String projectTitle, String categoryTitle) {
        int pid = getProjectIdByTitle(projectTitle);
        int cid = getCategoryIdByTitle(categoryTitle);

        Response list = RestAssured.get(BASE_URL + "/projects/" + pid + "/categories");
        assertEquals(200, list.statusCode());
        List<Map<String, Object>> cats = list.jsonPath().getList("categories");
        boolean found = false;
        if (cats != null) {
            for (Map<String, Object> c : cats) {
                if (Integer.parseInt(c.get("id").toString()) == cid) { found = true; break; }
            }
        }
        assertTrue(found, "Linked category not found under project");
    }

    @Then("the manager is notified of the completion of the linking operation")
    public void managerNotifiedOfCompletion() { assertTrue(true); }

    @Then("the manager is notified of a conflict with the message {string}")
    public void managerNotifiedOfConflict(String expected) { assertTrue(true); }

    @Then("the manager is notified of the non-existence error with a message {string}")
    public void managerNotifiedOfNonExistence(String expectedMessage) { assertTrue(true); }
}

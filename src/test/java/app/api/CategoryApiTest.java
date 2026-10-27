package app;

import app.api.TestAuthHelper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CategoryApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void getAllCategoriesTest() {
        String token = TestAuthHelper.getAnnaToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/categories")
                .then()
                .statusCode(200);
    }

    @Test
    void createAndDeleteCategoryTest() {
        String token = TestAuthHelper.getAnnaToken();

        String body = """
                {
                  "name": "Test kategori",
                  "type": "EXPENSE"
                }
                """;

        Integer id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test kategori"))
                .body("type", equalTo("EXPENSE"))
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/categories/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    void categoriesWithoutTokenTest() {
        given()
                .when()
                .get("/api/categories")
                .then()
                .statusCode(401);
    }
}
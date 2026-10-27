package app.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class SubcategoryApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void getAllSubcategoriesTest() {
        String token = TestAuthHelper.getAnnaToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/subcategories")
                .then()
                .statusCode(200);
    }

    @Test
    void createAndDeleteSubcategoryTest() {
        String token = TestAuthHelper.getAnnaToken();

        String body = """
                {
                  "name": "Test underkategori",
                  "category": {
                    "id": 13
                  }
                }
                """;

        Integer id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/subcategories")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test underkategori"))
                .extract()
                .path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/subcategories/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    void subcategoriesWithoutTokenTest() {
        given()
                .when()
                .get("/api/subcategories")
                .then()
                .statusCode(401);
    }
}
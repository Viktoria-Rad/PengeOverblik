package app.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class SubcategoryApiTest extends BaseApiTest {

    private static String token;
    private static Integer categoryId;

    @BeforeAll
    static void setupTestData() {

        token = TestAuthHelper.registerAndGetAnnaToken();

        String categoryBody = """
                {
                  "name": "Test kategori",
                  "type": "EXPENSE"
                }
                """;

        categoryId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(categoryBody)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void getAllSubcategoriesTest() {

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/subcategories")
                .then()
                .statusCode(200);
    }

    @Test
    void createAndDeleteSubcategoryTest() {

        String body = """
                {
                  "name": "Test underkategori",
                  "category": {
                    "id": %d
                  }
                }
                """.formatted(categoryId);

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
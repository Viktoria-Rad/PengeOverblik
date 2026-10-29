package app.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BudgetApiTest extends BaseApiTest {

    private static String token;
    private static Integer subcategoryId;

    @BeforeAll
    static void setupTestData() {

        token = TestAuthHelper.registerAndGetAnnaToken();

        String categoryBody = """
                {
                  "name": "Mad",
                  "type": "EXPENSE"
                }
                """;

        Integer categoryId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(categoryBody)
                .when()
                .post("/api/categories")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        String subcategoryBody = """
        {
          "name": "Dagligvarer",
          "category": {
            "id": %d
          }
        }
        """.formatted(categoryId);

        subcategoryId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(subcategoryBody)
                .when()
                .post("/api/subcategories")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void getAllBudgetsTest() {

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/budgets")
                .then()
                .statusCode(200);
    }

    @Test
    void createUpdateAndDeleteBudgetTest() {

        String createBody = """
                {
                  "amount": 1200.00,
                  "month": 12,
                  "year": 2026,
                  "subcategoryId": %d
                }
                """.formatted(subcategoryId);

        Integer id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createBody)
                .when()
                .post("/api/budgets")
                .then()
                .statusCode(201)
                .body("amount", equalTo(1200.00f))
                .body("month", equalTo(12))
                .body("year", equalTo(2026))
                .extract()
                .path("id");

        String updateBody = """
                {
                  "amount": 1500.00,
                  "month": 12,
                  "year": 2026,
                  "subcategoryId": %d
                }
                """.formatted(subcategoryId);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/budgets/" + id)
                .then()
                .statusCode(200)
                .body("amount", equalTo(1500.00f));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/budgets/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    void budgetsWithoutTokenTest() {

        given()
                .when()
                .get("/api/budgets")
                .then()
                .statusCode(401);
    }
}
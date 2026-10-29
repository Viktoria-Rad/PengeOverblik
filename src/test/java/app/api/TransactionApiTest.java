package app.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TransactionApiTest extends BaseApiTest {

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
    void getAllTransactionsTest() {

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/transactions")
                .then()
                .statusCode(200);
    }

    @Test
    void createUpdateAndDeleteTransactionTest() {

        String createBody = """
                {
                  "amount": 75.50,
                  "date": "2026-10-27",
                  "type": "EXPENSE",
                  "description": "API test køb",
                  "subcategoryId": %d
                }
                """.formatted(subcategoryId);

        Integer id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createBody)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(201)
                .body("description", equalTo("API test køb"))
                .body("subcategoryId", equalTo(subcategoryId))
                .extract()
                .path("id");

        String updateBody = """
                {
                  "amount": 99.50,
                  "date": "2026-10-27",
                  "type": "EXPENSE",
                  "description": "API test køb opdateret",
                  "subcategoryId": %d
                }
                """.formatted(subcategoryId);

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/transactions/" + id)
                .then()
                .statusCode(200)
                .body(
                        "description",
                        equalTo("API test køb opdateret")
                )
                .body(
                        "subcategoryId",
                        equalTo(subcategoryId)
                );

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/transactions/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    void transactionsWithoutTokenTest() {

        given()
                .when()
                .get("/api/transactions")
                .then()
                .statusCode(401);
    }
}
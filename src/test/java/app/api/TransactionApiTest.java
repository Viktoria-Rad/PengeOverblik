package app.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class TransactionApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void getAllTransactionsTest() {
        String token = TestAuthHelper.getAnnaToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/transactions")
                .then()
                .statusCode(200);
    }

    @Test
    void createUpdateAndDeleteTransactionTest() {
        String token = TestAuthHelper.getAnnaToken();

        String createBody = """
                {
                  "amount": 75.50,
                  "date": "2026-10-27",
                  "type": "EXPENSE",
                  "description": "API test køb",
                  "subcategoryId": 13
                }
                """;

        Integer id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createBody)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(201)
                .body("description", equalTo("API test køb"))
                .body("subcategoryId", equalTo(13))
                .extract()
                .path("id");

        String updateBody = """
                {
                  "amount": 99.50,
                  "date": "2026-10-27",
                  "type": "EXPENSE",
                  "description": "API test køb opdateret",
                  "subcategoryId": 13
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/transactions/" + id)
                .then()
                .statusCode(200)
                .body("description", equalTo("API test køb opdateret"));

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
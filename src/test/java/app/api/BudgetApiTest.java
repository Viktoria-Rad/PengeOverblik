package app.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class BudgetApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void getAllBudgetsTest() {
        String token = TestAuthHelper.getAnnaToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/budgets")
                .then()
                .statusCode(200);
    }

    @Test
    void createUpdateAndDeleteBudgetTest() {
        String token = TestAuthHelper.getAnnaToken();

        String createBody = """
                {
                  "amount": 1200.00,
                  "month": 12,
                  "year": 2026,
                  "subcategoryId": 13
                }
                """;

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
                  "subcategoryId": 13
                }
                """;

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
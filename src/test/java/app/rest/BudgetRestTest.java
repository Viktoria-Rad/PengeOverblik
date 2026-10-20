package app.rest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BudgetRestTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void testGetAllBudgets() {
        given()
                .when()
                .get("/api/budgets")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }
    @Test
    void testGetBudgetById() {
        given()
                .when()
                .get("/api/budgets/4")
                .then()
                .statusCode(200)
                .body("id", equalTo(4))
                .body("amount", equalTo(4500.0f))
                .body("subcategoryName", equalTo("Tøj & sko"));
    }
    @Test
    void testCreateBudget() {

        String json = """
            {
              "amount": 2750.00,
              "month": 11,
              "year": 2026,
              "subcategoryId": 1
            }
            """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/api/budgets")
                .then()
                .statusCode(201)
                .body("amount", equalTo(2750.0f))
                .body("month", equalTo(11))
                .body("year", equalTo(2026))
                .body("subcategoryId", equalTo(1));
    }
    @Test
    void testUpdateBudget() {

        String json = """
            {
              "amount": 3200.00,
              "month": 8,
              "year": 2026,
              "subcategoryId": 2
            }
            """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .put("/api/budgets/4")
                .then()
                .statusCode(200)
                .body("id", equalTo(4))
                .body("amount", equalTo(3200.0f))
                .body("month", equalTo(8))
                .body("year", equalTo(2026))
                .body("subcategoryId", equalTo(2))
                .body("subcategoryName", equalTo("Tøj & sko"));
    }
    @Test
    void testDeleteBudget() {

        given()
                .when()
                .delete("/api/budgets/4")
                .then()
                .statusCode(204);
    }
}
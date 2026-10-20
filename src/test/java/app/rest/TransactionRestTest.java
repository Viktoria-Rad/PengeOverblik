package app.rest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TransactionRestTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void testGetAllTransactions() {
        given()
                .when()
                .get("/api/transactions")
                .then()
                .statusCode(200)
                .body("$", not(empty()));
    }

    @Test
    void testGetTransactionById() {
        given()
                .when()
                .get("/api/transactions/5")
                .then()
                .statusCode(200)
                .body("id", equalTo(5))
                .body("description", equalTo("Løn"));
    }

    @Test
    void testCreateTransaction() {
        String json = """
            {
              "amount": 199.95,
              "date": "2026-10-07",
              "type": "EXPENSE",
              "description": "REST Assured test",
              "subcategoryId": 1
            }
            """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/api/transactions")
                .then()
                .statusCode(201)
                .body("amount", equalTo(199.95f))
                .body("type", equalTo("EXPENSE"))
                .body("description", equalTo("REST Assured test"))
                .body("subcategoryId", equalTo(1));
    }

    @Test
    void testUpdateTransaction() {

        String json = """
            {
              "amount": 22000.00,
              "date": "2026-08-20",
              "type": "INCOME",
              "description": "REST update test",
              "subcategoryId": 8
            }
            """;

        given()
                .contentType("application/json")
                .body(json)
                .when()
                .put("/api/transactions/5")
                .then()
                .statusCode(200)
                .body("id", equalTo(5))
                .body("description", equalTo("REST update test"))
                .body("type", equalTo("INCOME"));
    }

    @Test
    void testDeleteTransaction() {

        given()
                .when()
                .delete("/api/transactions/3")
                .then()
                .statusCode(204);
    }
}
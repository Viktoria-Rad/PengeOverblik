package app.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class RecurringPaymentApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void getAllRecurringPaymentsTest() {
        String token = TestAuthHelper.getAnnaToken();

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/recurring-payments")
                .then()
                .statusCode(200);
    }

    @Test
    void createUpdateAndDeleteRecurringPaymentTest() {
        String token = TestAuthHelper.getAnnaToken();

        String createBody = """
                {
                  "name": "Test abonnement",
                  "amount": 129.00,
                  "type": "EXPENSE",
                  "frequency": "MONTHLY",
                  "startDate": "2026-10-27",
                  "nextPaymentDate": "2026-11-27",
                  "subcategory": {
                    "id": 13
                  }
                }
                """;

        Integer id = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(createBody)
                .when()
                .post("/api/recurring-payments")
                .then()
                .statusCode(201)
                .body("name", equalTo("Test abonnement"))
                .extract()
                .path("id");

        String updateBody = """
                {
                  "name": "Test abonnement opdateret",
                  "amount": 149.00,
                  "type": "EXPENSE",
                  "frequency": "MONTHLY",
                  "startDate": "2026-10-27",
                  "nextPaymentDate": "2026-11-27",
                  "subcategory": {
                    "id": 13
                  }
                }
                """;

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(updateBody)
                .when()
                .put("/api/recurring-payments/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Test abonnement opdateret"));

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/api/recurring-payments/" + id)
                .then()
                .statusCode(204);
    }

    @Test
    void recurringPaymentsWithoutTokenTest() {
        given()
                .when()
                .get("/api/recurring-payments")
                .then()
                .statusCode(401);
    }
}
package app.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class DashboardApiTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 7070;
    }

    @Test
    void getDashboardTest() {
        String token = TestAuthHelper.getAnnaToken();

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("month", 10)
                .queryParam("year", 2026)
                .when()
                .get("/api/dashboard")
                .then()
                .statusCode(200)
                .body("dashboard", notNullValue())
                .body("budgets", notNullValue())
                .body("budgetOverview", notNullValue())
                .body("expensesByCategory", notNullValue())
                .body("monthlyFinanceOverview", notNullValue())
                .body("month", equalTo(10))
                .body("year", equalTo(2026));
    }

    @Test
    void dashboardWithoutTokenTest() {
        given()
                .queryParam("month", 10)
                .queryParam("year", 2026)
                .when()
                .get("/api/dashboard")
                .then()
                .statusCode(401);
    }
}
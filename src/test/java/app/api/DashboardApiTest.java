package app.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class DashboardApiTest extends BaseApiTest {

    private static String token;

    @BeforeAll
    static void setupTestData() {
        token = TestAuthHelper.registerAndGetAnnaToken();
    }

    @Test
    void getDashboardTest() {

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
package app.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class SecurityApiTest extends BaseApiTest {

    @BeforeAll
    static void createTestUser() {

        String body = """
                {
                  "name": "Anna",
                  "email": "anna@test.dk",
                  "password": "123456"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(201);
    }

    @Test
    void loginTest() {

        String body = """
                {
                  "email": "anna@test.dk",
                  "password": "123456"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("email", equalTo("anna@test.dk"))
                .body("name", equalTo("Anna"));
    }

    @Test
    void wrongPasswordTest() {

        String body = """
                {
                  "email": "anna@test.dk",
                  "password": "forkert-password"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void meWithoutTokenTest() {

        given()
                .when()
                .get("/auth/me")
                .then()
                .statusCode(401);
    }
}
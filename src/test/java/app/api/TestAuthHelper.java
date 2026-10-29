package app.api;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class TestAuthHelper {

    private static final String EMAIL = "anna@test.dk";
    private static final String PASSWORD = "123456";

    public static void registerAnna() {

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

    public static String getAnnaToken() {

        String body = """
                {
                  "email": "anna@test.dk",
                  "password": "123456"
                }
                """;

        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    public static String registerAndGetAnnaToken() {

        registerAnna();

        return getAnnaToken();
    }
}
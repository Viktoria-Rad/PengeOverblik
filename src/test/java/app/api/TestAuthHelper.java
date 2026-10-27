package app.api;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;

public class TestAuthHelper {

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
}
package app.routes;

import app.controllers.ApiTestController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ApiTestRoutes {

    private final ApiTestController apiTestController;

    public void register(Javalin app) {
        app.get("/api/test", apiTestController::testApi);
        app.get("/api/currency-test", apiTestController::testCurrency);
    }
}
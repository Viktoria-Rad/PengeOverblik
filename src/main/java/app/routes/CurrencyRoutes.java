package app.routes;

import app.controllers.CurrencyController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CurrencyRoutes {

    private final CurrencyController currencyController;

    public void register(Javalin app) {
        app.get("/api/currency", currencyController::getExchangeRate);
    }
}
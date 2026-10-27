package app.controllers;

import app.services.CurrencyService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    public void getExchangeRate(Context ctx)
            throws IOException, InterruptedException {

        String to = ctx.queryParam("to") != null
                ? ctx.queryParam("to").toUpperCase()
                : "DKK";

        if (to.equals("DKK")) {
            ctx.json(Map.of(
                    "currency", "DKK",
                    "rate", BigDecimal.ONE
            ));
            return;
        }

        BigDecimal rate =
                currencyService.getExchangeRate("DKK", to);

        ctx.json(Map.of(
                "currency", to,
                "rate", rate
        ));
    }
}
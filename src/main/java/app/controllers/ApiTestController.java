package app.controllers;

import app.services.CurrencyService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@RequiredArgsConstructor
public class ApiTestController {

    private final CurrencyService currencyService;

    public void testApi(Context ctx) {

        ctx.json(Map.of(
                "message", "Hej fra PengeOverblik!",
                "status", "API virker"
        ));
    }

    public void testCurrency(Context ctx)
            throws IOException, InterruptedException {

        BigDecimal amount =
                new BigDecimal("40000");

        BigDecimal convertedAmount =
                currencyService.convert(
                        amount,
                        "DKK",
                        "UAH"
                );

        ctx.json(Map.of(
                "from", "DKK",
                "to", "UAH",
                "amount", amount,
                "convertedAmount", convertedAmount
        ));
    }
}
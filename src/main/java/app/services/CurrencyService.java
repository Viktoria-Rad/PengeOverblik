package app.services;

import app.dtos.ExchangeRateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CurrencyService {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BigDecimal getExchangeRate(String from, String to)
            throws IOException, InterruptedException {

        String url =
                "https://api.frankfurter.dev/v2/rate/"
                        + from + "/" + to;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        ExchangeRateResponse exchangeRate =
                objectMapper.readValue(
                        response.body(),
                        ExchangeRateResponse.class
                );

        return exchangeRate.getRate();
    }
    public BigDecimal convert(
            BigDecimal amount,
            String from,
            String to)
            throws IOException, InterruptedException {

        BigDecimal rate = getExchangeRate(from, to);

        return amount
                .multiply(rate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
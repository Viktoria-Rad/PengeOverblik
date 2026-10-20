package app.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class ExchangeRateResponse {

    private String date;
    private String base;
    private String quote;
    private BigDecimal rate;
}
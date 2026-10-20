package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class MonthlyFinanceOverview {

    private String monthName;
    private int month;
    private int year;
    private BigDecimal income;
    private BigDecimal expenses;
}
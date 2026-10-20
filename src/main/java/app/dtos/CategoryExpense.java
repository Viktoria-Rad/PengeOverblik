package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CategoryExpense {

    private String categoryName;
    private BigDecimal amount;
    private int percentage;
}
package app.dtos;

import java.math.BigDecimal;

public record BudgetDTO(
        Integer id,
        BigDecimal amount,
        Integer month,
        Integer year,
        Integer subcategoryId,
        String subcategoryName
) {}
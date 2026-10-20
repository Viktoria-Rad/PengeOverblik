package app.dtos;

import java.math.BigDecimal;

public record CreateBudgetDTO(
        BigDecimal amount,
        Integer month,
        Integer year,
        Integer subcategoryId
) {}
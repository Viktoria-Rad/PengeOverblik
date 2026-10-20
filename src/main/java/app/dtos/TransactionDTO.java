package app.dtos;

import app.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDTO(
        Integer id,
        BigDecimal amount,
        LocalDate date,
        TransactionType type,
        String description,
        Integer subcategoryId,
        String subcategoryName
) {
}
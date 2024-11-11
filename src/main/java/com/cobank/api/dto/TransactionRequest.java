package com.cobank.api.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(

        // Range is because of international iban standards
        @NotNull @Size(min = 15, max = 34) String iban,
        @NotNull TransactionType type,
        @NotNull @Positive @DecimalMin(value = "0.01", message = "Transaction amount must be greater than zero")
        BigDecimal amount
) {
}

package com.cobank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        String iban,
        TransactionType transactionType,
        BigDecimal amount,
        BigDecimal resultingBalance,
        LocalDateTime timestamp,
        String description
) {}

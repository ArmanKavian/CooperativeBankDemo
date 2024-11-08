package com.cobank.api.dto;

import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        String iban,
        TransactionType transactionType,
        double amount,
        double resultingBalance,
        LocalDateTime timestamp,
        String description
) {}

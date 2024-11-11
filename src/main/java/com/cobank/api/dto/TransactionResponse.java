package com.cobank.api.dto;

import java.math.BigDecimal;

public record TransactionResponse(String iban, BigDecimal newBalance, String description) {
}

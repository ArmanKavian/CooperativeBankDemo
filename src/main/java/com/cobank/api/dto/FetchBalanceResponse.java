package com.cobank.api.dto;

import java.math.BigDecimal;

public record FetchBalanceResponse(String iban, BigDecimal balance) {
}

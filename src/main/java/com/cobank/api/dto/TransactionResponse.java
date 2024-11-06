package com.cobank.api.dto;

public record TransactionResponse(String iban, double newBalance, String description) {
}

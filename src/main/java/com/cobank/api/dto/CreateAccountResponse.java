package com.cobank.api.dto;

public record CreateAccountResponse(
        String id,
        String iban,
        String address
) {
}

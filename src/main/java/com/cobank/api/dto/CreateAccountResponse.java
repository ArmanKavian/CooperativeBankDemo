package com.cobank.api.dto;

import java.util.UUID;

public record CreateAccountResponse(
        UUID id,
        String iban,
        String address
) {
}

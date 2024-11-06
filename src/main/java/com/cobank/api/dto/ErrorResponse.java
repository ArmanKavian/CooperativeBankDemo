package com.cobank.api.dto;

public record ErrorResponse(
        String errorCode,
        String message
) {
}

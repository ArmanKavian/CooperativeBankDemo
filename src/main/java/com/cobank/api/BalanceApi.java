package com.cobank.api;

import com.cobank.api.dto.ErrorResponse;
import com.cobank.api.dto.FetchBalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface BalanceApi {
    @Operation(summary = "Fetch account balance",
            description = "Retrieves the current balance of an account using the IBAN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = FetchBalanceResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Account not found",
                    content = @Content(mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @io.swagger.v3.oas.annotations.media.Schema(
                                    implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/accounts/balance/{iban}")
    ResponseEntity<FetchBalanceResponse> getBalance(
            @Parameter(description = "The IBAN of the account to retrieve the balance for", example = "NL00COOP1234567890")
            @PathVariable String iban);
}

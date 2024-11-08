package com.cobank.api;

import com.cobank.api.dto.TransactionHistoryResponse;
import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import com.cobank.api.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface TransactionApi {

    @Operation(summary = "Process a transaction",
            description = "Handles deposits and withdrawals for a specified account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/transactions")
    ResponseEntity<TransactionResponse> processTransaction(
            @Parameter(description =
                    "Transaction details including IBAN, type (DEPOSIT or WITHDRAWAL), and amount", required = true)
            @Valid @RequestBody TransactionRequest request);

    @Operation(summary = "Retrieve transaction history", description =
            "Fetches a paginated transaction history for the specified IBAN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(responseCode = "404", description = "IBAN not found",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/accounts/{iban}/transactions")
    ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
            @Parameter(description =
                    "IBAN of the account to fetch transaction history", required = true, example = "NL00COOP1234567890")
            @PathVariable String iban,
            @Parameter(description = "Page number for pagination (default is 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size for pagination (default is 10)")
            @RequestParam(defaultValue = "10") int size);
}
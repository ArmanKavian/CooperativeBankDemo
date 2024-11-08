package com.cobank.api;

import com.cobank.api.dto.TransactionHistoryResponse;
import com.cobank.api.dto.TransactionRequest;
import com.cobank.api.dto.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface TransactionApi {

    @PostMapping("/transactions")
    ResponseEntity<TransactionResponse> processTransaction(@Valid @RequestBody TransactionRequest request);

    @GetMapping("/accounts/{iban}/transactions")
    ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable String iban,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);
}